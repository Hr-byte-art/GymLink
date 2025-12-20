package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.mapper.*;
import com.ldr.gymlink.model.dto.review.AddCourseReviewRequest;
import com.ldr.gymlink.model.dto.review.CourseReviewQueryRequest;
import com.ldr.gymlink.model.entity.*;
import com.ldr.gymlink.model.vo.CourseReviewVo;
import com.ldr.gymlink.service.CourseReviewService;
import com.ldr.gymlink.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程评价服务实现
 */
@Service
public class CourseReviewServiceImpl extends ServiceImpl<CourseReviewMapper, CourseReview>
        implements CourseReviewService {

    @Resource
    private CourseOrderMapper courseOrderMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private CoachMapper coachMapper;

    @Resource
    private CoachAppointmentMapper coachAppointmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseReviewVo addReview(AddCourseReviewRequest request) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        ThrowUtils.throwIf(request.getStudentId() == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        ThrowUtils.throwIf(request.getRating() == null || request.getRating() < 1 || request.getRating() > 5,
                ErrorCode.PARAMS_ERROR, "评分必须在1-5之间");

        Long studentId = request.getStudentId();
        Integer reviewType = request.getReviewType() != null ? request.getReviewType() : 1;

        CourseReview review = new CourseReview();
        review.setStudentId(studentId);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setCreateTime(new Date());
        review.setReviewType(reviewType);

        if (reviewType == 1) {
            // 课程评价
            Long courseId = request.getCourseId();
            ThrowUtils.throwIf(courseId == null, ErrorCode.PARAMS_ERROR, "课程ID不能为空");

            // 检查是否可以评价
            if (!canReview(studentId, courseId)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "您未购买该课程或已评价过");
            }

            // 获取课程信息，获取教练ID
            Course course = courseMapper.selectById(courseId);
            ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");

            review.setCourseId(courseId);
            review.setCoachId(course.getCoachId());
        } else if (reviewType == 2) {
            // 预约评价
            Long appointmentId = request.getAppointmentId();
            ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "预约ID不能为空");

            // 检查是否可以评价预约
            if (!canReviewAppointment(studentId, appointmentId)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "该预约不存在、未完成或已评价过");
            }

            // 获取预约信息
            CoachAppointment appointment = coachAppointmentMapper.selectById(appointmentId);
            ThrowUtils.throwIf(appointment == null, ErrorCode.NOT_FOUND_ERROR, "预约不存在");

            review.setAppointmentId(appointmentId);
            review.setCoachId(appointment.getCoachId());
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的评价类型");
        }

        boolean save = this.save(review);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评价失败，请稍后重试");
        }

        // 返回评价信息
        return convertToVo(review);
    }

    @Override
    public Page<CourseReviewVo> listReviewPage(CourseReviewQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询20条");

        // 构建查询条件
        LambdaQueryWrapper<CourseReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(request.getCoachId() != null, CourseReview::getCoachId, request.getCoachId());
        queryWrapper.eq(request.getCourseId() != null, CourseReview::getCourseId, request.getCourseId());
        queryWrapper.eq(request.getStudentId() != null, CourseReview::getStudentId, request.getStudentId());
        queryWrapper.eq(request.getRating() != null, CourseReview::getRating, request.getRating());
        queryWrapper.orderByDesc(CourseReview::getCreateTime);

        Page<CourseReview> page = this.page(Page.of(pageNum, pageSize), queryWrapper);

        // 批量获取学员和课程信息
        List<CourseReview> records = page.getRecords();
        if (records.isEmpty()) {
            Page<CourseReviewVo> voPage = new Page<>(pageNum, pageSize);
            voPage.setTotal(0);
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        Set<Long> studentIds = records.stream().map(CourseReview::getStudentId).collect(Collectors.toSet());
        Set<Long> courseIds = records.stream().map(CourseReview::getCourseId).collect(Collectors.toSet());

        Map<Long, Student> studentMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<Student> students = studentMapper.selectBatchIds(studentIds);
            studentMap = students.stream().collect(Collectors.toMap(Student::getId, s -> s, (a, b) -> a));
        }

        Map<Long, Course> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            List<Course> courses = courseMapper.selectBatchIds(courseIds);
            courseMap = courses.stream().collect(Collectors.toMap(Course::getId, c -> c, (a, b) -> a));
        }

        // 转换为VO
        Map<Long, Student> finalStudentMap = studentMap;
        Map<Long, Course> finalCourseMap = courseMap;
        List<CourseReviewVo> voList = records.stream().map(review -> {
            CourseReviewVo vo = new CourseReviewVo();
            BeanUtils.copyProperties(review, vo);

            Student student = finalStudentMap.get(review.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getName());
                vo.setStudentAvatar(student.getAvatar());
            }

            Course course = finalCourseMap.get(review.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getName());
            }

            return vo;
        }).collect(Collectors.toList());

        Page<CourseReviewVo> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(page.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public boolean canReview(Long studentId, Long courseId) {
        // 1. 检查是否购买了该课程
        LambdaQueryWrapper<CourseOrder> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(CourseOrder::getStudentId, studentId)
                .eq(CourseOrder::getCourseId, courseId)
                .eq(CourseOrder::getStatus, 1); // 已支付
        Long orderCount = courseOrderMapper.selectCount(orderQuery);
        if (orderCount == 0) {
            return false;
        }

        // 2. 检查是否已经评价过
        LambdaQueryWrapper<CourseReview> reviewQuery = new LambdaQueryWrapper<>();
        reviewQuery.eq(CourseReview::getStudentId, studentId)
                .eq(CourseReview::getCourseId, courseId)
                .eq(CourseReview::getReviewType, 1);
        Long reviewCount = this.count(reviewQuery);
        return reviewCount == 0;
    }

    /**
     * 检查是否可以评价预约
     */
    @Override
    public boolean canReviewAppointment(Long studentId, Long appointmentId) {
        // 1. 检查预约是否存在且已确认（status=1表示已确认）
        LambdaQueryWrapper<CoachAppointment> appointmentQuery = new LambdaQueryWrapper<>();
        appointmentQuery.eq(CoachAppointment::getId, appointmentId)
                .eq(CoachAppointment::getStudentId, studentId)
                .eq(CoachAppointment::getStatus, 1); // 已确认
        Long appointmentCount = coachAppointmentMapper.selectCount(appointmentQuery);
        if (appointmentCount == 0) {
            return false;
        }

        // 2. 检查是否已经评价过
        LambdaQueryWrapper<CourseReview> reviewQuery = new LambdaQueryWrapper<>();
        reviewQuery.eq(CourseReview::getStudentId, studentId)
                .eq(CourseReview::getAppointmentId, appointmentId)
                .eq(CourseReview::getReviewType, 2);
        Long reviewCount = this.count(reviewQuery);
        return reviewCount == 0;
    }

    @Override
    public Map<String, Object> getCoachReviewStats(Long coachId) {
        Map<String, Object> stats = new HashMap<>();

        // 查询该教练的所有评价
        LambdaQueryWrapper<CourseReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseReview::getCoachId, coachId);
        List<CourseReview> reviews = this.list(queryWrapper);

        if (reviews.isEmpty()) {
            stats.put("avgRating", 0.0);
            stats.put("reviewCount", 0);
            stats.put("ratingDistribution", new HashMap<Integer, Long>());
            return stats;
        }

        // 计算平均评分
        double avgRating = reviews.stream()
                .mapToInt(CourseReview::getRating)
                .average()
                .orElse(0.0);
        // 保留一位小数
        avgRating = BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP).doubleValue();

        // 评分分布
        Map<Integer, Long> ratingDistribution = reviews.stream()
                .collect(Collectors.groupingBy(CourseReview::getRating, Collectors.counting()));

        stats.put("avgRating", avgRating);
        stats.put("reviewCount", reviews.size());
        stats.put("ratingDistribution", ratingDistribution);
        return stats;
    }

    @Override
    public Integer getCoachStudentCount(Long coachId) {
        Set<Long> studentIds = new HashSet<>();

        // 1. 通过课程订单统计购买了该教练课程的学员
        LambdaQueryWrapper<CourseOrder> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(CourseOrder::getCoachId, coachId)
                .eq(CourseOrder::getStatus, 1) // 已支付
                .select(CourseOrder::getStudentId);
        List<CourseOrder> orders = courseOrderMapper.selectList(orderQuery);
        orders.forEach(order -> studentIds.add(order.getStudentId()));

        // 2. 通过预约记录统计预约了该教练并已确认的学员
        LambdaQueryWrapper<CoachAppointment> appointmentQuery = new LambdaQueryWrapper<>();
        appointmentQuery.eq(CoachAppointment::getCoachId, coachId)
                .eq(CoachAppointment::getStatus, 1) // 已确认
                .select(CoachAppointment::getStudentId);
        List<CoachAppointment> appointments = coachAppointmentMapper.selectList(appointmentQuery);
        appointments.forEach(appointment -> studentIds.add(appointment.getStudentId()));

        // 返回去重后的学员数
        return studentIds.size();
    }

    @Override
    public Integer getCoachCourseCount(Long coachId) {
        // 统计该教练的课程数
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getCoachId, coachId);
        return Math.toIntExact(courseMapper.selectCount(queryWrapper));
    }

    @Override
    public Map<String, Object> getCourseReviewStats(Long courseId) {
        Map<String, Object> stats = new HashMap<>();

        // 查询该课程的所有评价
        LambdaQueryWrapper<CourseReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseReview::getCourseId, courseId)
                .eq(CourseReview::getReviewType, 1); // 只统计课程评价
        List<CourseReview> reviews = this.list(queryWrapper);

        if (reviews.isEmpty()) {
            stats.put("avgRating", 0.0);
            stats.put("reviewCount", 0);
            stats.put("ratingDistribution", new HashMap<Integer, Long>());
            return stats;
        }

        // 计算平均评分
        double avgRating = reviews.stream()
                .mapToInt(CourseReview::getRating)
                .average()
                .orElse(0.0);
        // 保留一位小数
        avgRating = BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP).doubleValue();

        // 评分分布
        Map<Integer, Long> ratingDistribution = reviews.stream()
                .collect(Collectors.groupingBy(CourseReview::getRating, Collectors.counting()));

        stats.put("avgRating", avgRating);
        stats.put("reviewCount", reviews.size());
        stats.put("ratingDistribution", ratingDistribution);
        return stats;
    }

    /**
     * 转换为VO
     */
    private CourseReviewVo convertToVo(CourseReview review) {
        CourseReviewVo vo = new CourseReviewVo();
        BeanUtils.copyProperties(review, vo);

        // 获取学员信息
        Student student = studentMapper.selectById(review.getStudentId());
        if (student != null) {
            vo.setStudentName(student.getName());
            vo.setStudentAvatar(student.getAvatar());
        }

        // 获取课程信息
        Course course = courseMapper.selectById(review.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }

        return vo;
    }
}

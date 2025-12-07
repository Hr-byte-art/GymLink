package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.course.AddCourseRequest;
import com.ldr.gymlink.model.dto.course.CourseQueryPageRequest;
import com.ldr.gymlink.model.dto.course.UpdateCourseRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.Course;
import com.ldr.gymlink.model.entity.CourseOrder;
import com.ldr.gymlink.model.vo.CourseStatisticsVo;
import com.ldr.gymlink.model.vo.CourseVo;
import com.ldr.gymlink.service.CourseService;
import com.ldr.gymlink.mapper.CoachMapper;
import com.ldr.gymlink.mapper.CourseMapper;
import com.ldr.gymlink.mapper.CourseOrderMapper;
import com.ldr.gymlink.utils.ThrowUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 木子宸
 * @description 针对表【course(健身课程表)】的数据库操作Service实现
 * @createDate 2025-11-30 20:57:06
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course>
        implements CourseService {

    @jakarta.annotation.Resource
    private CourseOrderMapper courseOrderMapper;

    @jakarta.annotation.Resource
    private CoachMapper coachMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseVo addCourse(AddCourseRequest addCourseRequest) {
        ThrowUtils.throwIf(addCourseRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        Course course = new Course();
        BeanUtils.copyProperties(addCourseRequest, course);
        course.setCreateTime(new Date());
        boolean save = this.save(course);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加课程失败，请稍后重试");
        }

        CourseVo courseVo = new CourseVo();
        BeanUtils.copyProperties(course, courseVo);
        return courseVo;
    }

    @Override
    public Page<CourseVo> listCoursePage(CourseQueryPageRequest courseQueryPageRequest) {
        ThrowUtils.throwIf(courseQueryPageRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int pageSize = courseQueryPageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个课程");
        int pageNum = courseQueryPageRequest.getPageNum();

        LambdaQueryWrapper<Course> queryWrapper = getCourseQueryWrapper(courseQueryPageRequest);
        Page<Course> page = this.page(Page.of(pageNum, pageSize), queryWrapper);

        Page<CourseVo> courseVoPage = new Page<>(pageNum, pageSize);
        courseVoPage.setTotal(page.getTotal());
        courseVoPage.setRecords(page.getRecords().stream().map(course -> {
            CourseVo courseVo = new CourseVo();
            BeanUtils.copyProperties(course, courseVo);
            return courseVo;
        }).toList());
        return courseVoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourse(Integer id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "课程id不能为空");
        Course course = this.getById(id);
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        // 删除课程记录
        return this.removeById(id);
    }

    @Override
    public boolean updateCourse(Integer id, UpdateCourseRequest updateCourseRequest) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "课程id不能为空");
        Course course = this.getById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }
        BeanUtils.copyProperties(updateCourseRequest, course);
        return this.updateById(course);
    }

    @Override
    public CourseVo getCourseById(Integer id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "课程id不能为空");
        Course course = this.getById(id);
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        CourseVo courseVo = new CourseVo();
        BeanUtils.copyProperties(course, courseVo);
        return courseVo;
    }

    @Override
    public CourseStatisticsVo getCourseStatistics() {
        CourseStatisticsVo vo = new CourseStatisticsVo();

        // 1. 课程总数
        vo.setTotalCourse(this.count());

        // 2. 获取时间范围
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStart = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date monthStart = calendar.getTime();

        // 3. 订单统计（只统计已支付的订单 status=1）
        LambdaQueryWrapper<CourseOrder> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(CourseOrder::getStatus, 1).ge(CourseOrder::getCreateTime, todayStart);
        vo.setTodayOrderCount(courseOrderMapper.selectCount(todayQuery));

        LambdaQueryWrapper<CourseOrder> weekQuery = new LambdaQueryWrapper<>();
        weekQuery.eq(CourseOrder::getStatus, 1).ge(CourseOrder::getCreateTime, weekStart);
        vo.setWeekOrderCount(courseOrderMapper.selectCount(weekQuery));

        LambdaQueryWrapper<CourseOrder> monthQuery = new LambdaQueryWrapper<>();
        monthQuery.eq(CourseOrder::getStatus, 1).ge(CourseOrder::getCreateTime, monthStart);
        vo.setMonthOrderCount(courseOrderMapper.selectCount(monthQuery));

        // 4. 销售额统计
        LambdaQueryWrapper<CourseOrder> allPaidQuery = new LambdaQueryWrapper<>();
        allPaidQuery.eq(CourseOrder::getStatus, 1);
        List<CourseOrder> allPaidOrders = courseOrderMapper.selectList(allPaidQuery);
        BigDecimal totalRevenue = allPaidOrders.stream()
                .map(CourseOrder::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalRevenue(totalRevenue);

        LambdaQueryWrapper<CourseOrder> monthPaidQuery = new LambdaQueryWrapper<>();
        monthPaidQuery.eq(CourseOrder::getStatus, 1).ge(CourseOrder::getCreateTime, monthStart);
        List<CourseOrder> monthPaidOrders = courseOrderMapper.selectList(monthPaidQuery);
        BigDecimal monthRevenue = monthPaidOrders.stream()
                .map(CourseOrder::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setMonthRevenue(monthRevenue);

        // 5. 难度分布统计
        List<Course> allCourses = this.list();
        Map<String, Long> difficultyMap = allCourses.stream()
                .filter(c -> StringUtils.isNotBlank(c.getDifficulty()))
                .collect(Collectors.groupingBy(Course::getDifficulty, Collectors.counting()));
        Map<String, String> difficultyNameMap = Map.of(
                "beginner", "初级",
                "intermediate", "中级",
                "advanced", "高级"
        );
        List<CourseStatisticsVo.DifficultyCountVo> difficultyStats = difficultyMap.entrySet().stream()
                .map(entry -> {
                    CourseStatisticsVo.DifficultyCountVo dv = new CourseStatisticsVo.DifficultyCountVo();
                    dv.setDifficulty(entry.getKey());
                    dv.setDifficultyName(difficultyNameMap.getOrDefault(entry.getKey(), entry.getKey()));
                    dv.setCount(entry.getValue());
                    return dv;
                }).collect(Collectors.toList());
        vo.setDifficultyStatistics(difficultyStats);

        // 6. 类型分布统计
        Map<String, Long> typeMap = allCourses.stream()
                .filter(c -> StringUtils.isNotBlank(c.getType()))
                .collect(Collectors.groupingBy(Course::getType, Collectors.counting()));
        List<CourseStatisticsVo.TypeCountVo> typeStats = typeMap.entrySet().stream()
                .map(entry -> {
                    CourseStatisticsVo.TypeCountVo tv = new CourseStatisticsVo.TypeCountVo();
                    tv.setType(entry.getKey());
                    tv.setTypeName(entry.getKey());
                    tv.setCount(entry.getValue());
                    return tv;
                }).collect(Collectors.toList());
        vo.setTypeStatistics(typeStats);

        // 7. 近7天购买趋势
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<CourseStatisticsVo.DailyCountVo> dailyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date dayStart = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date dayEnd = cal.getTime();

            LambdaQueryWrapper<CourseOrder> dayQuery = new LambdaQueryWrapper<>();
            dayQuery.eq(CourseOrder::getStatus, 1)
                    .ge(CourseOrder::getCreateTime, dayStart)
                    .lt(CourseOrder::getCreateTime, dayEnd);
            List<CourseOrder> dayOrders = courseOrderMapper.selectList(dayQuery);

            CourseStatisticsVo.DailyCountVo dcv = new CourseStatisticsVo.DailyCountVo();
            dcv.setDate(sdf.format(dayStart));
            dcv.setCount((long) dayOrders.size());
            dcv.setRevenue(dayOrders.stream()
                    .map(CourseOrder::getPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            dailyTrend.add(dcv);
        }
        vo.setDailyOrderTrend(dailyTrend);

        // 8. 热门课程TOP10
        Map<Long, List<CourseOrder>> courseOrderMap = allPaidOrders.stream()
                .filter(o -> o.getCourseId() != null)
                .collect(Collectors.groupingBy(CourseOrder::getCourseId));
        Map<Long, String> courseNameMap = allCourses.stream()
                .collect(Collectors.toMap(Course::getId, Course::getName, (a, b) -> a));
        List<CourseStatisticsVo.CourseRankVo> hotCourses = courseOrderMap.entrySet().stream()
                .map(entry -> {
                    CourseStatisticsVo.CourseRankVo crv = new CourseStatisticsVo.CourseRankVo();
                    crv.setCourseId(entry.getKey());
                    crv.setCourseName(courseNameMap.getOrDefault(entry.getKey(), "未知课程"));
                    crv.setOrderCount((long) entry.getValue().size());
                    crv.setTotalRevenue(entry.getValue().stream()
                            .map(CourseOrder::getPrice)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return crv;
                })
                .sorted((a, b) -> Long.compare(b.getOrderCount(), a.getOrderCount()))
                .limit(10)
                .collect(Collectors.toList());
        vo.setHotCourseRank(hotCourses);

        // 9. 热门教练TOP10
        Map<Long, List<CourseOrder>> coachOrderMap = allPaidOrders.stream()
                .filter(o -> o.getCoachId() != null)
                .collect(Collectors.groupingBy(CourseOrder::getCoachId));
        List<Coach> allCoaches = coachMapper.selectList(null);
        Map<Long, String> coachNameMap = allCoaches.stream()
                .collect(Collectors.toMap(Coach::getId, Coach::getName, (a, b) -> a));
        List<CourseStatisticsVo.CoachRankVo> hotCoaches = coachOrderMap.entrySet().stream()
                .map(entry -> {
                    CourseStatisticsVo.CoachRankVo crv = new CourseStatisticsVo.CoachRankVo();
                    crv.setCoachId(entry.getKey());
                    crv.setCoachName(coachNameMap.getOrDefault(entry.getKey(), "未知教练"));
                    crv.setOrderCount((long) entry.getValue().size());
                    crv.setTotalRevenue(entry.getValue().stream()
                            .map(CourseOrder::getPrice)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return crv;
                })
                .sorted((a, b) -> Long.compare(b.getOrderCount(), a.getOrderCount()))
                .limit(10)
                .collect(Collectors.toList());
        vo.setHotCoachRank(hotCoaches);

        return vo;
    }

    /**
     * 获取课程查询条件
     *
     * @param courseQueryPageRequest 查询条件
     * @return 查询条件
     */
    private LambdaQueryWrapper<Course> getCourseQueryWrapper(CourseQueryPageRequest courseQueryPageRequest) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        if (courseQueryPageRequest == null) {
            return queryWrapper;
        }

        String name = courseQueryPageRequest.getName();
        Integer coachId = courseQueryPageRequest.getCoachId();
        String difficulty = courseQueryPageRequest.getDifficulty();
        String type = courseQueryPageRequest.getType();
        // 模糊查询课程名称
        queryWrapper.like(StringUtils.isNotBlank(name), Course::getName, name);
        // 精确查询教练ID
        queryWrapper.eq(coachId != null, Course::getCoachId, coachId);
        // 精确查询难度等级
        queryWrapper.eq(StringUtils.isNotBlank(difficulty), Course::getDifficulty, difficulty);
        // 价格范围查询
        queryWrapper.ge(courseQueryPageRequest.getMinPrice() != null,
                Course::getPrice, courseQueryPageRequest.getMinPrice());
        queryWrapper.le(courseQueryPageRequest.getMaxPrice() != null,
                Course::getPrice, courseQueryPageRequest.getMaxPrice());
        // 时长范围查询
        queryWrapper.ge(courseQueryPageRequest.getMinDuration() != null,
                Course::getDuration, courseQueryPageRequest.getMinDuration());
        queryWrapper.le(courseQueryPageRequest.getMaxDuration() != null,
                Course::getDuration, courseQueryPageRequest.getMaxDuration());

        queryWrapper.eq(courseQueryPageRequest.getType() != null, Course::getType, type );
        return queryWrapper;
    }
}

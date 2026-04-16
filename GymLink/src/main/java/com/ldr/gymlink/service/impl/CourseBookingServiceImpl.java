package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.mapper.CourseBookingMapper;
import com.ldr.gymlink.model.dto.course.BookCourseScheduleRequest;
import com.ldr.gymlink.model.dto.course.StudentCourseBookingQueryRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.Course;
import com.ldr.gymlink.model.entity.CourseBooking;
import com.ldr.gymlink.model.entity.CourseOrder;
import com.ldr.gymlink.model.entity.CourseSchedule;
import com.ldr.gymlink.model.vo.CourseBookingVo;
import com.ldr.gymlink.service.CoachService;
import com.ldr.gymlink.service.CourseBookingService;
import com.ldr.gymlink.service.CourseOrderService;
import com.ldr.gymlink.service.CourseScheduleService;
import com.ldr.gymlink.service.CourseService;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CourseBookingServiceImpl extends ServiceImpl<CourseBookingMapper, CourseBooking>
        implements CourseBookingService {

    @Resource
    private CourseScheduleService courseScheduleService;

    @Resource
    private CourseOrderService courseOrderService;

    @Resource
    private CourseService courseService;

    @Resource
    private CoachService coachService;

    @Resource
    private StudentService studentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bookCourseSchedule(BookCourseScheduleRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "约课参数不能为空");
        ThrowUtils.throwIf(request.getStudentId() == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        ThrowUtils.throwIf(request.getScheduleId() == null, ErrorCode.PARAMS_ERROR, "排期ID不能为空");
        ThrowUtils.throwIf(studentService.getById(request.getStudentId()) == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");

        CourseSchedule schedule = courseScheduleService.getById(request.getScheduleId());
        ThrowUtils.throwIf(schedule == null, ErrorCode.NOT_FOUND_ERROR, "团课排期不存在");
        ThrowUtils.throwIf(schedule.getStatus() == null || schedule.getStatus() != 1,
                ErrorCode.OPERATION_ERROR, "当前排期不可报名");
        ThrowUtils.throwIf(schedule.getStartTime() == null || !schedule.getStartTime().after(new Date()),
                ErrorCode.OPERATION_ERROR, "已开始或已结束的排期不可报名");

        Course course = courseService.getById(schedule.getCourseId());
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        ThrowUtils.throwIf(course.getDeliveryMode() == null || course.getDeliveryMode() != 2,
                ErrorCode.OPERATION_ERROR, "当前课程不是团体课");

        LambdaQueryWrapper<CourseBooking> duplicateQuery = new LambdaQueryWrapper<CourseBooking>()
                .eq(CourseBooking::getStudentId, request.getStudentId())
                .eq(CourseBooking::getScheduleId, request.getScheduleId())
                .eq(CourseBooking::getStatus, 1);
        ThrowUtils.throwIf(this.count(duplicateQuery) > 0, ErrorCode.OPERATION_ERROR, "您已报名该场团课");

        int bookedCount = schedule.getBookedCount() == null ? 0 : schedule.getBookedCount();
        int capacity = schedule.getCapacity() == null ? 0 : schedule.getCapacity();
        ThrowUtils.throwIf(bookedCount >= capacity, ErrorCode.OPERATION_ERROR, "当前团课已满员");

        CourseOrder availableOrder = getAvailableGroupCourseOrder(request.getStudentId(), schedule.getCourseId());
        ThrowUtils.throwIf(availableOrder == null, ErrorCode.OPERATION_ERROR, "请先购买该团体课程或剩余课次不足");

        availableOrder.setRemainingSessions(availableOrder.getRemainingSessions() - 1);
        boolean updateOrder = courseOrderService.updateById(availableOrder);
        ThrowUtils.throwIf(!updateOrder, ErrorCode.SYSTEM_ERROR, "扣减课程课次失败");

        schedule.setBookedCount(bookedCount + 1);
        boolean updateSchedule = courseScheduleService.updateById(schedule);
        ThrowUtils.throwIf(!updateSchedule, ErrorCode.SYSTEM_ERROR, "更新排期人数失败");

        CourseBooking booking = new CourseBooking();
        booking.setStudentId(request.getStudentId());
        booking.setCourseId(schedule.getCourseId());
        booking.setCoachId(schedule.getCoachId());
        booking.setScheduleId(schedule.getId());
        booking.setOrderId(availableOrder.getId());
        booking.setStatus(1);
        booking.setCreateTime(new Date());
        return this.save(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelCourseBooking(Long bookingId) {
        ThrowUtils.throwIf(bookingId == null, ErrorCode.PARAMS_ERROR, "约课记录ID不能为空");
        CourseBooking booking = this.getById(bookingId);
        ThrowUtils.throwIf(booking == null, ErrorCode.NOT_FOUND_ERROR, "约课记录不存在");
        ThrowUtils.throwIf(booking.getStatus() == null || booking.getStatus() != 1,
                ErrorCode.OPERATION_ERROR, "当前约课状态不允许取消");

        CourseSchedule schedule = courseScheduleService.getById(booking.getScheduleId());
        ThrowUtils.throwIf(schedule == null, ErrorCode.NOT_FOUND_ERROR, "团课排期不存在");
        ThrowUtils.throwIf(schedule.getStartTime() != null && !schedule.getStartTime().after(new Date()),
                ErrorCode.OPERATION_ERROR, "团课已开始，无法取消约课");

        CourseOrder order = courseOrderService.getById(booking.getOrderId());
        if (order != null) {
            int remainingSessions = order.getRemainingSessions() == null ? 0 : order.getRemainingSessions();
            int totalSessions = order.getTotalSessions() == null ? remainingSessions + 1 : order.getTotalSessions();
            order.setRemainingSessions(Math.min(remainingSessions + 1, totalSessions));
            courseOrderService.updateById(order);
        }

        int bookedCount = schedule.getBookedCount() == null ? 0 : schedule.getBookedCount();
        schedule.setBookedCount(Math.max(bookedCount - 1, 0));
        courseScheduleService.updateById(schedule);

        booking.setStatus(2);
        return this.updateById(booking);
    }

    @Override
    public Page<CourseBookingVo> listStudentCourseBookingPage(StudentCourseBookingQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "查询参数不能为空");
        ThrowUtils.throwIf(request.getStudentId() == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");

        LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<CourseBooking>()
                .eq(CourseBooking::getStudentId, request.getStudentId());
        if (request.getStatus() != null) {
            queryWrapper.eq(CourseBooking::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(CourseBooking::getCreateTime);

        Page<CourseBooking> page = this.page(Page.of(request.getPageNum(), request.getPageSize()), queryWrapper);
        Page<CourseBookingVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(buildCourseBookingVoList(page.getRecords()));
        return voPage;
    }

    private CourseOrder getAvailableGroupCourseOrder(Long studentId, Long courseId) {
        LambdaQueryWrapper<CourseOrder> queryWrapper = new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getStudentId, studentId)
                .eq(CourseOrder::getCourseId, courseId)
                .eq(CourseOrder::getDeliveryMode, 2)
                .eq(CourseOrder::getStatus, 1)
                .gt(CourseOrder::getRemainingSessions, 0)
                .orderByAsc(CourseOrder::getCreateTime)
                .last("limit 1");
        return courseOrderService.getOne(queryWrapper, false);
    }

    private List<CourseBookingVo> buildCourseBookingVoList(List<CourseBooking> bookings) {
        return bookings.stream().map(booking -> {
            CourseBookingVo vo = new CourseBookingVo();
            BeanUtils.copyProperties(booking, vo);
            Course course = courseService.getById(booking.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getName());
            }
            Coach coach = coachService.getById(booking.getCoachId());
            if (coach != null) {
                vo.setCoachName(coach.getName());
            }
            CourseSchedule schedule = courseScheduleService.getById(booking.getScheduleId());
            if (schedule != null) {
                vo.setScheduleStartTime(schedule.getStartTime());
                vo.setScheduleEndTime(schedule.getEndTime());
            }
            return vo;
        }).toList();
    }
}

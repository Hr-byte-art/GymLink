package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.mapper.CourseBookingMapper;
import com.ldr.gymlink.mapper.CourseScheduleMapper;
import com.ldr.gymlink.model.dto.course.AddCourseScheduleRequest;
import com.ldr.gymlink.model.dto.course.CourseScheduleQueryRequest;
import com.ldr.gymlink.model.dto.course.UpdateCourseScheduleRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.Course;
import com.ldr.gymlink.model.entity.CourseBooking;
import com.ldr.gymlink.model.entity.CourseSchedule;
import com.ldr.gymlink.model.vo.CourseScheduleVo;
import com.ldr.gymlink.service.CoachService;
import com.ldr.gymlink.service.CourseScheduleService;
import com.ldr.gymlink.service.CourseService;
import com.ldr.gymlink.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CourseScheduleServiceImpl extends ServiceImpl<CourseScheduleMapper, CourseSchedule>
        implements CourseScheduleService {

    private static final long ONE_MINUTE_MILLIS = 60 * 1000L;

    @Resource
    private CourseService courseService;

    @Resource
    private CoachService coachService;

    @Resource
    private CourseBookingMapper courseBookingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseSchedule addCourseSchedule(AddCourseScheduleRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "排期参数不能为空");
        ThrowUtils.throwIf(request.getCourseId() == null, ErrorCode.PARAMS_ERROR, "课程ID不能为空");
        ThrowUtils.throwIf(request.getStartTime() == null || request.getEndTime() == null,
                ErrorCode.PARAMS_ERROR, "开始时间和结束时间不能为空");
        ThrowUtils.throwIf(request.getCapacity() == null || request.getCapacity() <= 0,
                ErrorCode.PARAMS_ERROR, "人数上限必须大于0");
        ThrowUtils.throwIf(!request.getEndTime().after(request.getStartTime()),
                ErrorCode.PARAMS_ERROR, "结束时间必须晚于开始时间");

        Course course = courseService.getById(request.getCourseId());
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        ThrowUtils.throwIf(course.getDeliveryMode() == null || course.getDeliveryMode() != 2,
                ErrorCode.OPERATION_ERROR, "只有团体课程可以创建排期");
        validateCourseScheduleDurationLimit(course, null, request.getStartTime(), request.getEndTime());

        CourseSchedule courseSchedule = new CourseSchedule();
        courseSchedule.setCourseId(course.getId());
        courseSchedule.setCoachId(course.getCoachId());
        courseSchedule.setStartTime(request.getStartTime());
        courseSchedule.setEndTime(request.getEndTime());
        courseSchedule.setCapacity(request.getCapacity());
        courseSchedule.setBookedCount(0);
        courseSchedule.setStatus(1);
        courseSchedule.setCreateTime(new Date());
        boolean saved = this.save(courseSchedule);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "创建团课排期失败");
        return courseSchedule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCourseSchedule(Long id, UpdateCourseScheduleRequest request) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "排期ID不能为空");
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "更新参数不能为空");

        CourseSchedule schedule = this.getById(id);
        ThrowUtils.throwIf(schedule == null, ErrorCode.NOT_FOUND_ERROR, "排期不存在");
        Course course = courseService.getById(schedule.getCourseId());
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");

        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }
        ThrowUtils.throwIf(schedule.getStartTime() == null || schedule.getEndTime() == null
                        || !schedule.getEndTime().after(schedule.getStartTime()),
                ErrorCode.PARAMS_ERROR, "结束时间必须晚于开始时间");
        validateCourseScheduleDurationLimit(course, id, schedule.getStartTime(), schedule.getEndTime());

        if (request.getCapacity() != null) {
            ThrowUtils.throwIf(request.getCapacity() <= 0, ErrorCode.PARAMS_ERROR, "人数上限必须大于0");
            ThrowUtils.throwIf(schedule.getBookedCount() != null && request.getCapacity() < schedule.getBookedCount(),
                    ErrorCode.PARAMS_ERROR, "人数上限不能小于已报名人数");
            schedule.setCapacity(request.getCapacity());
        }
        if (request.getStatus() != null) {
            schedule.setStatus(request.getStatus());
        }
        return this.updateById(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourseSchedule(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "排期ID不能为空");
        CourseSchedule schedule = this.getById(id);
        ThrowUtils.throwIf(schedule == null, ErrorCode.NOT_FOUND_ERROR, "排期不存在");

        LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<CourseBooking>()
                .eq(CourseBooking::getScheduleId, id)
                .eq(CourseBooking::getStatus, 1);
        long activeBookingCount = courseBookingMapper.selectCount(bookingQuery);
        ThrowUtils.throwIf(activeBookingCount > 0, ErrorCode.OPERATION_ERROR, "当前排期已有学员报名，无法删除");
        return this.removeById(id);
    }

    @Override
    public Page<CourseScheduleVo> listCourseSchedulePage(CourseScheduleQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "查询参数不能为空");
        LambdaQueryWrapper<CourseSchedule> queryWrapper = new LambdaQueryWrapper<>();
        if (request.getCourseId() != null) {
            queryWrapper.eq(CourseSchedule::getCourseId, request.getCourseId());
        }
        if (request.getStatus() != null) {
            queryWrapper.eq(CourseSchedule::getStatus, request.getStatus());
        }
        queryWrapper.orderByAsc(CourseSchedule::getStartTime);
        Page<CourseSchedule> page = this.page(Page.of(request.getPageNum(), request.getPageSize()), queryWrapper);
        return buildScheduleVoPage(page);
    }

    @Override
    public List<CourseScheduleVo> listAvailableSchedulesByCourse(Long courseId) {
        ThrowUtils.throwIf(courseId == null, ErrorCode.PARAMS_ERROR, "课程ID不能为空");
        LambdaQueryWrapper<CourseSchedule> queryWrapper = new LambdaQueryWrapper<CourseSchedule>()
                .eq(CourseSchedule::getCourseId, courseId)
                .eq(CourseSchedule::getStatus, 1)
                .ge(CourseSchedule::getStartTime, new Date())
                .orderByAsc(CourseSchedule::getStartTime);
        return buildScheduleVoList(this.list(queryWrapper));
    }

    private Page<CourseScheduleVo> buildScheduleVoPage(Page<CourseSchedule> page) {
        Page<CourseScheduleVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(buildScheduleVoList(page.getRecords()));
        return voPage;
    }

    private List<CourseScheduleVo> buildScheduleVoList(List<CourseSchedule> schedules) {
        return schedules.stream().map(schedule -> {
            CourseScheduleVo vo = new CourseScheduleVo();
            BeanUtils.copyProperties(schedule, vo);
            Course course = courseService.getById(schedule.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getName());
            }
            Coach coach = coachService.getById(schedule.getCoachId());
            if (coach != null) {
                vo.setCoachName(coach.getName());
            }
            int bookedCount = schedule.getBookedCount() == null ? 0 : schedule.getBookedCount();
            int capacity = schedule.getCapacity() == null ? 0 : schedule.getCapacity();
            vo.setRemainingCapacity(Math.max(capacity - bookedCount, 0));
            return vo;
        }).toList();
    }

    private void validateCourseScheduleDurationLimit(Course course, Long currentScheduleId, Date startTime, Date endTime) {
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        Integer courseDuration = course.getDuration();
        ThrowUtils.throwIf(courseDuration == null || courseDuration <= 0,
                ErrorCode.PARAMS_ERROR, "课程时长配置无效，无法创建排期");

        long scheduleDurationMillis = endTime.getTime() - startTime.getTime();
        int extraBufferMinutes = getExtraBufferMinutes(courseDuration);
        long maximumDurationMillis = (courseDuration + extraBufferMinutes) * ONE_MINUTE_MILLIS;
        long existingDurationMillis = list(new LambdaQueryWrapper<CourseSchedule>()
                .eq(CourseSchedule::getCourseId, course.getId())
                .ne(currentScheduleId != null, CourseSchedule::getId, currentScheduleId))
                .stream()
                .filter(item -> item.getStartTime() != null && item.getEndTime() != null
                        && item.getEndTime().after(item.getStartTime()))
                .mapToLong(item -> item.getEndTime().getTime() - item.getStartTime().getTime())
                .sum();
        long totalDurationMillis = existingDurationMillis + scheduleDurationMillis;

        ThrowUtils.throwIf(totalDurationMillis > maximumDurationMillis,
                ErrorCode.PARAMS_ERROR,
                String.format("当前课程排期累计总时长不能超过%d分钟：课程%d分钟 + 缓冲%d分钟，当前已排%d分钟，本次提交后将达到%d分钟",
                        courseDuration + extraBufferMinutes,
                        courseDuration,
                        extraBufferMinutes,
                        existingDurationMillis / ONE_MINUTE_MILLIS,
                        totalDurationMillis / ONE_MINUTE_MILLIS));
    }

    private int getExtraBufferMinutes(int courseDuration) {
        return Math.max(courseDuration / 60, 0) * 10;
    }
}

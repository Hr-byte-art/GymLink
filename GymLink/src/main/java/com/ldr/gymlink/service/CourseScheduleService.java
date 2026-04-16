package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.course.AddCourseScheduleRequest;
import com.ldr.gymlink.model.dto.course.CourseScheduleQueryRequest;
import com.ldr.gymlink.model.dto.course.UpdateCourseScheduleRequest;
import com.ldr.gymlink.model.entity.CourseSchedule;
import com.ldr.gymlink.model.vo.CourseScheduleVo;

import java.util.List;

public interface CourseScheduleService extends IService<CourseSchedule> {
    CourseSchedule addCourseSchedule(AddCourseScheduleRequest request);

    boolean updateCourseSchedule(Long id, UpdateCourseScheduleRequest request);

    boolean deleteCourseSchedule(Long id);

    Page<CourseScheduleVo> listCourseSchedulePage(CourseScheduleQueryRequest request);

    List<CourseScheduleVo> listAvailableSchedulesByCourse(Long courseId);
}

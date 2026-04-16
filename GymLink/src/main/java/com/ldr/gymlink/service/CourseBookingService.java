package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.course.BookCourseScheduleRequest;
import com.ldr.gymlink.model.dto.course.StudentCourseBookingQueryRequest;
import com.ldr.gymlink.model.entity.CourseBooking;
import com.ldr.gymlink.model.vo.CourseBookingVo;

public interface CourseBookingService extends IService<CourseBooking> {
    boolean bookCourseSchedule(BookCourseScheduleRequest request);

    boolean cancelCourseBooking(Long bookingId);

    Page<CourseBookingVo> listStudentCourseBookingPage(StudentCourseBookingQueryRequest request);
}

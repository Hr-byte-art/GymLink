package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.coach.AllCoachAppointmentQueryRequest;
import com.ldr.gymlink.model.dto.coach.BookingCoachRequest;
import com.ldr.gymlink.model.dto.coach.CoachAppointmentSearchRequest;
import com.ldr.gymlink.model.dto.coach.CoachTimeAppointmentSearchRequest;
import com.ldr.gymlink.model.dto.coach.StudentCoachAppointmentQueryRequest;
import com.ldr.gymlink.model.dto.coach.StudentCoachTimeAppointmentSearchRequest;
import com.ldr.gymlink.model.dto.course.BookCourseScheduleRequest;
import com.ldr.gymlink.model.dto.course.StudentCourseBookingQueryRequest;
import com.ldr.gymlink.model.dto.equipment.AllEquipmentReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.BookingEquipmentRequest;
import com.ldr.gymlink.model.dto.equipment.EquipmentReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.EquipmentTimeRangeReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.StudentEquipmentReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.StudentEquipmentTimeRangeReservationQueryRequest;
import com.ldr.gymlink.model.vo.CoachAppointmentVo;
import com.ldr.gymlink.model.vo.CourseBookingVo;
import com.ldr.gymlink.model.vo.EquipmentReservationVo;
import com.ldr.gymlink.service.CoachService;
import com.ldr.gymlink.service.CourseBookingService;
import com.ldr.gymlink.service.EquipmentService;
import com.ldr.gymlink.utils.ResultUtils;
import com.ldr.gymlink.utils.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointment")
@Tag(name = "预约管理")
public class AppointmentController {

    @Resource
    private EquipmentService equipmentService;

    @Resource
    private CoachService coachService;

    @Resource
    private CourseBookingService courseBookingService;

    @PostMapping("/bookingEquipment")
    @Operation(summary = "学员预约器材")
    public BaseResponse<Boolean> bookingEquipment(@RequestBody BookingEquipmentRequest bookingEquipmentRequest) {
        ThrowUtils.throwIf(bookingEquipmentRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(bookingEquipmentRequest.getEquipmentId() == null, ErrorCode.PARAMS_ERROR, "请选择要预约的器材");
        ThrowUtils.throwIf(bookingEquipmentRequest.getStudentId() == null, ErrorCode.PARAMS_ERROR, "请选择预约学员");
        ThrowUtils.throwIf(bookingEquipmentRequest.getStartTime() == null, ErrorCode.PARAMS_ERROR, "请选择开始时间");
        ThrowUtils.throwIf(bookingEquipmentRequest.getEndTime() == null, ErrorCode.PARAMS_ERROR, "请选择结束时间");
        return ResultUtils.success(equipmentService.bookingEquipment(bookingEquipmentRequest));
    }

    @GetMapping("/cancelBookingEquipment")
    @Operation(summary = "取消器材预约")
    public BaseResponse<Boolean> cancelBookingEquipment(@RequestParam Long bookingId) {
        ThrowUtils.throwIf(bookingId == null, ErrorCode.PARAMS_ERROR, "请选择要取消的预约");
        return ResultUtils.success(equipmentService.cancelBookingEquipment(bookingId));
    }

    @PostMapping("/listStudentEquipmentReservation")
    @Operation(summary = "分页查询学员器材预约记录")
    public BaseResponse<Page<EquipmentReservationVo>> listStudentEquipmentReservation(
            @RequestBody StudentEquipmentReservationQueryRequest request) {
        return ResultUtils.success(equipmentService.listStudentReservationPage(request));
    }

    @PostMapping("/listAllEquipmentReservation")
    @Operation(summary = "分页查询全部器材预约记录")
    public BaseResponse<Page<EquipmentReservationVo>> listAllEquipmentReservation(
            @RequestBody AllEquipmentReservationQueryRequest request) {
        return ResultUtils.success(equipmentService.listAllReservationPage(request));
    }

    @PostMapping("/listEquipmentReservations")
    @Operation(summary = "分页查询指定器材预约记录")
    public BaseResponse<Page<EquipmentReservationVo>> listEquipmentReservations(
            @RequestBody EquipmentReservationQueryRequest request) {
        return ResultUtils.success(equipmentService.listReservationsByEquipment(request));
    }

    @PostMapping("/listEquipmentReservationsByTimeRange")
    @Operation(summary = "分页查询指定时间段器材预约记录")
    public BaseResponse<Page<EquipmentReservationVo>> listEquipmentReservationsByTimeRange(
            @RequestBody EquipmentTimeRangeReservationQueryRequest request) {
        return ResultUtils.success(equipmentService.listReservationsByTimeRange(request));
    }

    @PostMapping("/listStudentEquipmentReservationsByTimeRange")
    @Operation(summary = "分页查询学员指定时间段器材预约记录")
    public BaseResponse<Page<EquipmentReservationVo>> listStudentEquipmentReservationsByTimeRange(
            @RequestBody StudentEquipmentTimeRangeReservationQueryRequest request) {
        return ResultUtils.success(equipmentService.listStudentReservationsByTimeRange(request));
    }

    @PostMapping("/bookingCoach")
    @Operation(summary = "学员预约教练")
    public BaseResponse<Boolean> bookingCoach(@RequestBody BookingCoachRequest bookingCoachRequest) {
        ThrowUtils.throwIf(bookingCoachRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        return ResultUtils.success(coachService.bookingCoach(bookingCoachRequest));
    }

    @GetMapping("/cancelCoachAppointment")
    @Operation(summary = "取消教练预约")
    public BaseResponse<Boolean> cancelCoachAppointment(@RequestParam Long appointmentId) {
        ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "请选择要取消的预约");
        return ResultUtils.success(coachService.cancelCoachAppointment(appointmentId));
    }

    @PostMapping("/confirmCoachAppointment")
    @Operation(summary = "确认教练预约")
    public BaseResponse<Boolean> confirmCoachAppointment(@RequestParam Long appointmentId) {
        ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "请选择要确认的预约");
        return ResultUtils.success(coachService.confirmCoachAppointment(appointmentId));
    }

    @PostMapping("/rejectCoachAppointment")
    @Operation(summary = "拒绝教练预约")
    public BaseResponse<Boolean> rejectCoachAppointment(@RequestParam Long appointmentId) {
        ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "请选择要拒绝的预约");
        return ResultUtils.success(coachService.rejectCoachAppointment(appointmentId));
    }

    @PostMapping("/listStudentCoachAppointment")
    @Operation(summary = "分页查询学员教练预约记录")
    public BaseResponse<Page<CoachAppointmentVo>> listStudentCoachAppointment(
            @RequestBody StudentCoachAppointmentQueryRequest request) {
        return ResultUtils.success(coachService.listStudentCoachAppointmentPage(request));
    }

    @PostMapping("/listAllCoachAppointment")
    @Operation(summary = "分页查询全部教练预约记录")
    public BaseResponse<Page<CoachAppointmentVo>> listAllCoachAppointment(
            @RequestBody AllCoachAppointmentQueryRequest request) {
        return ResultUtils.success(coachService.listAllCoachAppointmentPage(request));
    }

    @PostMapping("/listAppointmentsByCoach")
    @Operation(summary = "分页查询指定教练预约记录")
    public BaseResponse<Page<CoachAppointmentVo>> listAppointmentsByCoach(
            @RequestBody CoachAppointmentSearchRequest request) {
        return ResultUtils.success(coachService.listAppointmentsByCoach(request));
    }

    @PostMapping("/listCoachAppointmentsByTimeRange")
    @Operation(summary = "分页查询指定时间段教练预约记录")
    public BaseResponse<Page<CoachAppointmentVo>> listCoachAppointmentsByTimeRange(
            @RequestBody CoachTimeAppointmentSearchRequest request) {
        return ResultUtils.success(coachService.listAppointmentsByTimeRange(request));
    }

    @PostMapping("/listStudentCoachAppointmentsByTimeRange")
    @Operation(summary = "分页查询学员指定时间段教练预约记录")
    public BaseResponse<Page<CoachAppointmentVo>> listStudentCoachAppointmentsByTimeRange(
            @RequestBody StudentCoachTimeAppointmentSearchRequest request) {
        return ResultUtils.success(coachService.listStudentAppointmentsByTimeRange(request));
    }

    @PostMapping("/bookCourseSchedule")
    @Operation(summary = "学员报名团课排期")
    public BaseResponse<Boolean> bookCourseSchedule(@RequestBody BookCourseScheduleRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        return ResultUtils.success(courseBookingService.bookCourseSchedule(request));
    }

    @GetMapping("/cancelCourseBooking")
    @Operation(summary = "取消团课报名")
    public BaseResponse<Boolean> cancelCourseBooking(@RequestParam Long bookingId) {
        ThrowUtils.throwIf(bookingId == null, ErrorCode.PARAMS_ERROR, "约课记录ID不能为空");
        return ResultUtils.success(courseBookingService.cancelCourseBooking(bookingId));
    }

    @PostMapping("/listStudentCourseBooking")
    @Operation(summary = "分页查询学员团课报名记录")
    public BaseResponse<Page<CourseBookingVo>> listStudentCourseBooking(
            @RequestBody StudentCourseBookingQueryRequest request) {
        return ResultUtils.success(courseBookingService.listStudentCourseBookingPage(request));
    }
}

package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.equipment.*;
import com.ldr.gymlink.model.dto.coach.*;
import com.ldr.gymlink.model.vo.CoachAppointmentVo;
import com.ldr.gymlink.model.vo.EquipmentReservationVo;
import com.ldr.gymlink.service.CoachService;

import com.ldr.gymlink.service.EquipmentService;
import com.ldr.gymlink.utils.ResultUtils;
import com.ldr.gymlink.utils.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @Author 王哈哈
 * @Date 2025/12/1 21:54:52
 * @Description 预约管理
 */
@RestController
@RequestMapping("/appointment")
@Tag(name = "预约管理")
public class AppointmentController {

    @Resource
    private EquipmentService equipmentService;

    @Resource
    private CoachService coachService;

    // region 健身器材预约

    @PostMapping("/bookingEquipment")
    @Operation(summary = "学员预约健身器材")
    public BaseResponse<Boolean> bookingEquipment(@RequestBody BookingEquipmentRequest bookingEquipmentRequest) {
        if (bookingEquipmentRequest == null) {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "请求参数为空，请检查后重试");
        }
        ThrowUtils.throwIf(bookingEquipmentRequest.getEquipmentId() == null, ErrorCode.PARAMS_ERROR, "请选择要预约的健身器材");
        ThrowUtils.throwIf(bookingEquipmentRequest.getStudentId() == null, ErrorCode.PARAMS_ERROR, "请选择要预约的学员");
        ThrowUtils.throwIf(bookingEquipmentRequest.getStartTime() == null, ErrorCode.PARAMS_ERROR, "请选择开始时间");
        ThrowUtils.throwIf(bookingEquipmentRequest.getEndTime() == null, ErrorCode.PARAMS_ERROR, "请选择结束时间");
        boolean booking = equipmentService.bookingEquipment(bookingEquipmentRequest);
        return ResultUtils.success(booking);
    }

    @GetMapping("/cancelBookingEquipment")
    @Operation(summary = "取消健身器材预约")
    public BaseResponse<Boolean> cancelBookingEquipment(@RequestParam Long bookingId) {
        ThrowUtils.throwIf(bookingId == null, ErrorCode.PARAMS_ERROR, "请选择要取消的预约");
        boolean cancelBooking = equipmentService.cancelBookingEquipment(bookingId);
        return ResultUtils.success(cancelBooking);
    }

    @PostMapping("/listStudentEquipmentReservation")
    @Operation(summary = "查询学员器材预约记录(分页)")
    public BaseResponse<Page<EquipmentReservationVo>> listStudentEquipmentReservation(
            @RequestBody StudentEquipmentReservationQueryRequest studentEquipmentReservationQueryRequest) {
        Page<EquipmentReservationVo> reservationVoPage = equipmentService
                .listStudentReservationPage(studentEquipmentReservationQueryRequest);
        return ResultUtils.success(reservationVoPage);
    }

    @PostMapping("/listAllEquipmentReservation")
    @Operation(summary = "查询所有器材预约记录(分页)")
    public BaseResponse<Page<EquipmentReservationVo>> listAllEquipmentReservation(
            @RequestBody AllEquipmentReservationQueryRequest allEquipmentReservationQueryRequest) {
        Page<EquipmentReservationVo> reservationVoPage = equipmentService
                .listAllReservationPage(allEquipmentReservationQueryRequest);
        return ResultUtils.success(reservationVoPage);
    }

    @PostMapping("/listEquipmentReservations")
    @Operation(summary = "查询某器材的预约记录(分页)")
    public BaseResponse<Page<EquipmentReservationVo>> listEquipmentReservations(
            @RequestBody EquipmentReservationQueryRequest request) {
        Page<EquipmentReservationVo> reservationVoPage = equipmentService.listReservationsByEquipment(request);
        return ResultUtils.success(reservationVoPage);
    }

    @PostMapping("/listEquipmentReservationsByTimeRange")
    @Operation(summary = "查询某时间段内的器材预约记录(分页)")
    public BaseResponse<Page<EquipmentReservationVo>> listEquipmentReservationsByTimeRange(
            @RequestBody EquipmentTimeRangeReservationQueryRequest request) {
        Page<EquipmentReservationVo> reservationVoPage = equipmentService.listReservationsByTimeRange(request);
        return ResultUtils.success(reservationVoPage);
    }

    @PostMapping("/listStudentEquipmentReservationsByTimeRange")
    @Operation(summary = "查询某学员在某时间段内的器材预约记录(分页)")
    public BaseResponse<Page<EquipmentReservationVo>> listStudentEquipmentReservationsByTimeRange(
            @RequestBody StudentEquipmentTimeRangeReservationQueryRequest request) {
        Page<EquipmentReservationVo> reservationVoPage = equipmentService.listStudentReservationsByTimeRange(request);
        return ResultUtils.success(reservationVoPage);
    }

    // endregion

    // region 教练预约

    @PostMapping("/bookingCoach")
    @Operation(summary = "学员预约教练")
    public BaseResponse<Boolean> bookingCoach(
            @RequestBody BookingCoachRequest bookingCoachRequest) {
        if (bookingCoachRequest == null) {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        boolean result = coachService.bookingCoach(bookingCoachRequest);
        return ResultUtils.success(result);
    }

    @GetMapping("/cancelCoachAppointment")
    @Operation(summary = "取消教练预约")
    public BaseResponse<Boolean> cancelCoachAppointment(@RequestParam Long appointmentId) {
        ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "请选择要取消的预约");
        boolean result = coachService.cancelCoachAppointment(appointmentId);
        return ResultUtils.success(result);
    }

    @PostMapping("/confirmCoachAppointment")
    @Operation(summary = "确认教练预约(教练操作)")
    public BaseResponse<Boolean> confirmCoachAppointment(@RequestParam Long appointmentId) {
        ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "请选择要确认的预约");
        boolean result = coachService.confirmCoachAppointment(appointmentId);
        return ResultUtils.success(result);
    }

    @PostMapping("/rejectCoachAppointment")
    @Operation(summary = "拒绝教练预约(教练操作)")
    public BaseResponse<Boolean> rejectCoachAppointment(@RequestParam Long appointmentId) {
        ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "请选择要拒绝的预约");
        boolean result = coachService.rejectCoachAppointment(appointmentId);
        return ResultUtils.success(result);
    }

    @PostMapping("/listStudentCoachAppointment")
    @Operation(summary = "查询学员教练预约记录(分页)")
    public BaseResponse<Page<CoachAppointmentVo>> listStudentCoachAppointment(
            @RequestBody StudentCoachAppointmentQueryRequest request) {
        Page<CoachAppointmentVo> page = coachService.listStudentCoachAppointmentPage(request);
        return ResultUtils.success(page);
    }

    @PostMapping("/listAllCoachAppointment")
    @Operation(summary = "查询所有教练预约记录(分页)")
    public BaseResponse<Page<CoachAppointmentVo>> listAllCoachAppointment(
            @RequestBody AllCoachAppointmentQueryRequest request) {
        Page<CoachAppointmentVo> page = coachService.listAllCoachAppointmentPage(request);
        return ResultUtils.success(page);
    }

    @PostMapping("/listAppointmentsByCoach")
    @Operation(summary = "查询某教练的预约记录(分页)")
    public BaseResponse<Page<CoachAppointmentVo>> listAppointmentsByCoach(
            @RequestBody CoachAppointmentSearchRequest request) {
        Page<CoachAppointmentVo> page = coachService.listAppointmentsByCoach(request);
        return ResultUtils.success(page);
    }

    @PostMapping("/listCoachAppointmentsByTimeRange")
    @Operation(summary = "查询某时间段内的教练预约记录(分页)")
    public BaseResponse<Page<CoachAppointmentVo>> listCoachAppointmentsByTimeRange(
            @RequestBody CoachTimeAppointmentSearchRequest request) {
        Page<CoachAppointmentVo> page = coachService.listAppointmentsByTimeRange(request);
        return ResultUtils.success(page);
    }

    @PostMapping("/listStudentCoachAppointmentsByTimeRange")
    @Operation(summary = "查询某学员在某时间段内的教练预约记录(分页)")
    public BaseResponse<Page<CoachAppointmentVo>> listStudentCoachAppointmentsByTimeRange(
            @RequestBody StudentCoachTimeAppointmentSearchRequest request) {
        Page<CoachAppointmentVo> page = coachService
                .listStudentAppointmentsByTimeRange(request);
        return ResultUtils.success(page);
    }

    // endregion
}

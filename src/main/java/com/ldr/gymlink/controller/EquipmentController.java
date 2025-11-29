package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.equipment.AddEquipmentRequest;
import com.ldr.gymlink.model.dto.equipment.AllReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.BookingEquipmentRequest;
import com.ldr.gymlink.model.dto.equipment.EquipmentQueryPageRequest;
import com.ldr.gymlink.model.dto.equipment.EquipmentReservationSearchRequest;
import com.ldr.gymlink.model.dto.equipment.StudentReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.StudentTimeReservationSearchRequest;
import com.ldr.gymlink.model.dto.equipment.TimeRangeReservationSearchRequest;
import com.ldr.gymlink.model.dto.equipment.UpdateEquipmentRequest;
import com.ldr.gymlink.model.vo.EquipmentReservationVo;
import com.ldr.gymlink.model.vo.EquipmentVo;
import com.ldr.gymlink.service.EquipmentService;
import com.ldr.gymlink.utils.ResultUtils;
import com.ldr.gymlink.utils.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 21:36:00
 * @Description 健身器材管理
 */
@RestController
@RequestMapping("/equipment")
@Tag(name = "健身器材管理")
public class EquipmentController {

    @Resource
    private EquipmentService equipmentService;

    @PostMapping("/addEquipment")
    @Operation(summary = "添加健身器材")
    public BaseResponse<EquipmentVo> addEquipment(@RequestBody AddEquipmentRequest addEquipmentRequest) {
        EquipmentVo equipmentVo = equipmentService.addEquipment(addEquipmentRequest);
        return ResultUtils.success(equipmentVo);
    }

    @PostMapping("/listEquipment")
    @Operation(summary = "分页查询健身器材信息")
    public BaseResponse<Page<EquipmentVo>> listEquipment(
            @RequestBody EquipmentQueryPageRequest equipmentQueryPageRequest) {
        Page<EquipmentVo> equipmentVos = equipmentService.listEquipmentPage(equipmentQueryPageRequest);
        return ResultUtils.success(equipmentVos);
    }

    @PostMapping("/deleteEquipment")
    @Operation(summary = "删除健身器材信息")
    public BaseResponse<Boolean> deleteEquipment(@RequestParam Long id) {
        boolean delete = equipmentService.deleteEquipment(id);
        return ResultUtils.success(delete);
    }

    @PostMapping("/updateEquipment")
    @Operation(summary = "更新健身器材信息")
    public BaseResponse<Boolean> updateEquipment(
            @RequestParam Long id,
            @RequestBody UpdateEquipmentRequest updateEquipmentRequest) {
        boolean update = equipmentService.updateEquipment(id, updateEquipmentRequest);
        return ResultUtils.success(update);
    }

    @GetMapping("/getEquipmentById")
    @Operation(summary = "获取健身器材信息")
    public BaseResponse<EquipmentVo> getEquipmentById(@RequestParam Long id) {
        EquipmentVo equipmentVo = equipmentService.getEquipmentById(id);
        return ResultUtils.success(equipmentVo);
    }

    @GetMapping("/bookingEquipment")
    @Operation(summary = "预约健身器材")
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
    @Operation(summary = "取消预约健身器材")
    public BaseResponse<Boolean> cancelBookingEquipment(@RequestParam Long bookingId) {
        ThrowUtils.throwIf(bookingId == null, ErrorCode.PARAMS_ERROR, "请选择要取消的预约");
        boolean cancelBooking = equipmentService.cancelBookingEquipment(bookingId);
        return ResultUtils.success(cancelBooking);
    }

    @PostMapping("/listStudentReservation")
    @Operation(summary = "获取学员预约记录")
    public BaseResponse<Page<EquipmentReservationVo>> listStudentReservation(
            @RequestBody StudentReservationQueryRequest studentReservationQueryRequest) {
        Page<EquipmentReservationVo> reservationVoPage = equipmentService
                .listStudentReservationPage(studentReservationQueryRequest);
        return ResultUtils.success(reservationVoPage);
    }

    @PostMapping("/listAllReservation")
    @Operation(summary = "获取所有预约记录")
    public BaseResponse<Page<EquipmentReservationVo>> listAllReservation(
            @RequestBody AllReservationQueryRequest allReservationQueryRequest) {
        Page<EquipmentReservationVo> reservationVoPage = equipmentService
                .listAllReservationPage(allReservationQueryRequest);
        return ResultUtils.success(reservationVoPage);
    }

    @PostMapping("/listReservationsByEquipment")
    @Operation(summary = "获取某器材的预约记录")
    public BaseResponse<Page<EquipmentReservationVo>> listReservationsByEquipment(
            @RequestBody EquipmentReservationSearchRequest request) {
        Page<EquipmentReservationVo> reservationVoPage = equipmentService.listReservationsByEquipment(request);
        return ResultUtils.success(reservationVoPage);
    }

    @PostMapping("/listReservationsByTimeRange")
    @Operation(summary = "获取某时间段内的预约记录")
    public BaseResponse<Page<EquipmentReservationVo>> listReservationsByTimeRange(
            @RequestBody TimeRangeReservationSearchRequest request) {
        Page<EquipmentReservationVo> reservationVoPage = equipmentService.listReservationsByTimeRange(request);
        return ResultUtils.success(reservationVoPage);
    }

    @PostMapping("/listStudentReservationsByTimeRange")
    @Operation(summary = "获取某学员在某时间段内的预约记录")
    public BaseResponse<Page<EquipmentReservationVo>> listStudentReservationsByTimeRange(
            @RequestBody StudentTimeReservationSearchRequest request) {
        Page<EquipmentReservationVo> reservationVoPage = equipmentService.listStudentReservationsByTimeRange(request);
        return ResultUtils.success(reservationVoPage);
    }
}

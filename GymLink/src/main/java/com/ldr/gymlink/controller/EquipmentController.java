package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.equipment.AddEquipmentRequest;
import com.ldr.gymlink.model.dto.equipment.EquipmentQueryPageRequest;
import com.ldr.gymlink.model.dto.equipment.UpdateEquipmentRequest;
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

    @PostMapping("/updateEquipmentImage")
    @Operation(summary = "修改器材图片")
    public BaseResponse<String> updateEquipmentImage(@RequestParam Long equipmentId, @RequestParam org.springframework.web.multipart.MultipartFile image) {
        ThrowUtils.throwIf(equipmentId == null, ErrorCode.PARAMS_ERROR, "器材id不能为空");
        String imageUrl = equipmentService.updateEquipmentImage(equipmentId, image);
        return ResultUtils.success(imageUrl);
    }

    @GetMapping("/getStatistics")
    @Operation(summary = "获取器材使用统计数据")
    public BaseResponse<com.ldr.gymlink.model.vo.EquipmentStatisticsVo> getStatistics() {
        com.ldr.gymlink.model.vo.EquipmentStatisticsVo statistics = equipmentService.getEquipmentStatistics();
        return ResultUtils.success(statistics);
    }
}

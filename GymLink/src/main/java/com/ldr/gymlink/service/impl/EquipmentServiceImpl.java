package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.mapper.EquipmentMapper;
import com.ldr.gymlink.model.dto.equipment.AddEquipmentRequest;
import com.ldr.gymlink.model.dto.equipment.AllEquipmentReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.BookingEquipmentRequest;
import com.ldr.gymlink.model.dto.equipment.EquipmentQueryPageRequest;
import com.ldr.gymlink.model.dto.equipment.EquipmentReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.StudentEquipmentReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.StudentEquipmentTimeRangeReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.EquipmentTimeRangeReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.UpdateEquipmentRequest;
import com.ldr.gymlink.model.entity.Equipment;
import com.ldr.gymlink.model.entity.EquipmentReservation;
import com.ldr.gymlink.model.vo.EquipmentReservationVo;
import com.ldr.gymlink.model.vo.EquipmentVo;
import com.ldr.gymlink.model.vo.StudentVo;
import com.ldr.gymlink.service.EquipmentReservationService;
import com.ldr.gymlink.service.EquipmentService;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 木子宸
 * @description 针对表【equipment(健身器材表)】的数据库操作Service实现
 * @createDate 2025-11-30 21:36:00
 */
@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment>
        implements EquipmentService {

    @Resource
    private EquipmentReservationService equipmentReservationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EquipmentVo addEquipment(AddEquipmentRequest addEquipmentRequest) {
        ThrowUtils.throwIf(addEquipmentRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        Equipment equipment = new Equipment();
        BeanUtils.copyProperties(addEquipmentRequest, equipment);
        equipment.setCreateTime(new Date());
        boolean save = this.save(equipment);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加健身器材失败，请稍后重试");
        }

        EquipmentVo equipmentVo = new EquipmentVo();
        BeanUtils.copyProperties(equipment, equipmentVo);
        return equipmentVo;
    }

    @Override
    public Page<EquipmentVo> listEquipmentPage(EquipmentQueryPageRequest equipmentQueryPageRequest) {
        ThrowUtils.throwIf(equipmentQueryPageRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int pageSize = equipmentQueryPageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个器材");
        int pageNum = equipmentQueryPageRequest.getPageNum();

        LambdaQueryWrapper<Equipment> queryWrapper = getEquipmentQueryWrapper(equipmentQueryPageRequest);
        Page<Equipment> page = this.page(Page.of(pageNum, pageSize), queryWrapper);

        Page<EquipmentVo> equipmentVoPage = new Page<>(pageNum, pageSize);
        equipmentVoPage.setTotal(page.getTotal());
        equipmentVoPage.setRecords(page.getRecords().stream().map(equipment -> {
            EquipmentVo equipmentVo = new EquipmentVo();
            BeanUtils.copyProperties(equipment, equipmentVo);
            return equipmentVo;
        }).toList());
        return equipmentVoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEquipment(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "健身器材id不能为空");
        Equipment equipment = this.getById(id);
        ThrowUtils.throwIf(equipment == null, ErrorCode.NOT_FOUND_ERROR, "健身器材不存在");

        // 删除健身器材记录
        return this.removeById(id);
    }

    @Override
    public boolean updateEquipment(Long id, UpdateEquipmentRequest updateEquipmentRequest) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "健身器材id不能为空");
        Equipment equipment = this.getById(id);
        if (equipment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "健身器材不存在");
        }
        BeanUtils.copyProperties(updateEquipmentRequest, equipment);
        return this.updateById(equipment);
    }

    @Override
    public EquipmentVo getEquipmentById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "健身器材id不能为空");
        Equipment equipment = this.getById(id);
        ThrowUtils.throwIf(equipment == null, ErrorCode.NOT_FOUND_ERROR, "健身器材不存在");
        EquipmentVo equipmentVo = new EquipmentVo();
        BeanUtils.copyProperties(equipment, equipmentVo);
        return equipmentVo;
    }

    @Override
    public boolean bookingEquipment(BookingEquipmentRequest bookingEquipmentRequest) {

        ThrowUtils.throwIf(bookingEquipmentRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long equipmentId = bookingEquipmentRequest.getEquipmentId();
        Long studentId = bookingEquipmentRequest.getStudentId();
        Date startTime = bookingEquipmentRequest.getStartTime();
        Date endTime = bookingEquipmentRequest.getEndTime();

        // 检查当前器材是否被预约
        LambdaQueryWrapper<EquipmentReservation> wrapper = new LambdaQueryWrapper<EquipmentReservation>()
                .eq(EquipmentReservation::getEquipmentId, equipmentId)
                .eq(EquipmentReservation::getStatus, 1);
        if (equipmentReservationService.count(wrapper) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前器材已被预约");
        }

        // 校验用户的所选时间段内是否预约了其余的器材

        LambdaQueryWrapper<EquipmentReservation> wrapper1 = new LambdaQueryWrapper<EquipmentReservation>()
                .eq(EquipmentReservation::getStudentId, studentId)
                .between(EquipmentReservation::getStartTime, startTime, endTime)
                .eq(EquipmentReservation::getStatus, 1);
        long count = equipmentReservationService.count(wrapper1);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前用户已预约其他器材");
        }

        // 检查用户当天是否预约了超过4个器材
        LambdaQueryWrapper<EquipmentReservation> wrapper2 = new LambdaQueryWrapper<EquipmentReservation>()
                .eq(EquipmentReservation::getStudentId, studentId)
                .eq(EquipmentReservation::getStatus, 1)
                .apply("DATE(create_time) = DATE(NOW())");
        long count1 = equipmentReservationService.count(wrapper2);
        if (count1 >= 4) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前用户已预约超过4个器材");
        }

        EquipmentReservation equipmentReservation = new EquipmentReservation();
        equipmentReservation.setStudentId(studentId);
        equipmentReservation.setEquipmentId(equipmentId);
        equipmentReservation.setStartTime(startTime);
        equipmentReservation.setEndTime(endTime);
        equipmentReservation.setStatus(1);
        equipmentReservation.setCreateTime(new Date());
        return equipmentReservationService.save(equipmentReservation);
    }

    @Resource
    private StudentService studentService;

    @Override
    public boolean cancelBookingEquipment(Long bookingId) {
        ThrowUtils.throwIf(bookingId == null, ErrorCode.PARAMS_ERROR, "预约id不能为空");
        EquipmentReservation equipmentReservation = equipmentReservationService.getById(bookingId);
        ThrowUtils.throwIf(equipmentReservation == null, ErrorCode.NOT_FOUND_ERROR, "预约记录不存在");
        return equipmentReservationService.removeById(bookingId);
    }

    @Override
    public Page<EquipmentReservationVo> listStudentReservationPage(
            StudentEquipmentReservationQueryRequest studentEquipmentReservationQueryRequest) {
        ThrowUtils.throwIf(studentEquipmentReservationQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long studentId = studentEquipmentReservationQueryRequest.getStudentId();
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");

        int pageSize = studentEquipmentReservationQueryRequest.getPageSize();
        int pageNum = studentEquipmentReservationQueryRequest.getPageNum();

        LambdaQueryWrapper<EquipmentReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EquipmentReservation::getStudentId, studentId);
        if (studentEquipmentReservationQueryRequest.getStatus() != null) {
            queryWrapper.eq(EquipmentReservation::getStatus, studentEquipmentReservationQueryRequest.getStatus());
        }
        queryWrapper.orderByDesc(EquipmentReservation::getCreateTime);

        Page<EquipmentReservation> page = equipmentReservationService.page(Page.of(pageNum, pageSize), queryWrapper);
        return getEquipmentReservationVoPage(page);
    }

    @Override
    public Page<EquipmentReservationVo> listAllReservationPage(
            AllEquipmentReservationQueryRequest allEquipmentReservationQueryRequest) {
        ThrowUtils.throwIf(allEquipmentReservationQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        int pageSize = allEquipmentReservationQueryRequest.getPageSize();
        int pageNum = allEquipmentReservationQueryRequest.getPageNum();

        LambdaQueryWrapper<EquipmentReservation> queryWrapper = new LambdaQueryWrapper<>();
        if (allEquipmentReservationQueryRequest.getStudentId() != null) {
            queryWrapper.eq(EquipmentReservation::getStudentId, allEquipmentReservationQueryRequest.getStudentId());
        }
        if (allEquipmentReservationQueryRequest.getEquipmentId() != null) {
            queryWrapper.eq(EquipmentReservation::getEquipmentId, allEquipmentReservationQueryRequest.getEquipmentId());
        }
        if (allEquipmentReservationQueryRequest.getStatus() != null) {
            queryWrapper.eq(EquipmentReservation::getStatus, allEquipmentReservationQueryRequest.getStatus());
        }
        queryWrapper.orderByDesc(EquipmentReservation::getCreateTime);

        Page<EquipmentReservation> page = equipmentReservationService.page(Page.of(pageNum, pageSize), queryWrapper);
        return getEquipmentReservationVoPage(page);
    }

    @Override
    public Page<EquipmentReservationVo> listReservationsByEquipment(EquipmentReservationQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long equipmentId = request.getEquipmentId();
        ThrowUtils.throwIf(equipmentId == null, ErrorCode.PARAMS_ERROR, "器材ID不能为空");

        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        LambdaQueryWrapper<EquipmentReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EquipmentReservation::getEquipmentId, equipmentId);
        if (request.getStatus() != null) {
            queryWrapper.eq(EquipmentReservation::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(EquipmentReservation::getCreateTime);

        Page<EquipmentReservation> page = equipmentReservationService.page(Page.of(pageNum, pageSize), queryWrapper);
        return getEquipmentReservationVoPage(page);
    }

    @Override
    public Page<EquipmentReservationVo> listReservationsByTimeRange(EquipmentTimeRangeReservationQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        ThrowUtils.throwIf(startTime == null || endTime == null, ErrorCode.PARAMS_ERROR, "时间段不能为空");

        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        LambdaQueryWrapper<EquipmentReservation> queryWrapper = new LambdaQueryWrapper<>();
        // 查询时间段有交集的预约： reservation.startTime < request.endTime AND reservation.endTime
        // > request.startTime
        queryWrapper.lt(EquipmentReservation::getStartTime, endTime);
        queryWrapper.gt(EquipmentReservation::getEndTime, startTime);

        if (request.getStatus() != null) {
            queryWrapper.eq(EquipmentReservation::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(EquipmentReservation::getCreateTime);

        Page<EquipmentReservation> page = equipmentReservationService.page(Page.of(pageNum, pageSize), queryWrapper);
        return getEquipmentReservationVoPage(page);
    }

    @Override
    public Page<EquipmentReservationVo> listStudentReservationsByTimeRange(
            StudentEquipmentTimeRangeReservationQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long studentId = request.getStudentId();
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        ThrowUtils.throwIf(startTime == null || endTime == null, ErrorCode.PARAMS_ERROR, "时间段不能为空");

        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        LambdaQueryWrapper<EquipmentReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EquipmentReservation::getStudentId, studentId);
        queryWrapper.lt(EquipmentReservation::getStartTime, endTime);
        queryWrapper.gt(EquipmentReservation::getEndTime, startTime);

        if (request.getStatus() != null) {
            queryWrapper.eq(EquipmentReservation::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(EquipmentReservation::getCreateTime);

        Page<EquipmentReservation> page = equipmentReservationService.page(Page.of(pageNum, pageSize), queryWrapper);
        return getEquipmentReservationVoPage(page);
    }

    private Page<EquipmentReservationVo> getEquipmentReservationVoPage(Page<EquipmentReservation> page) {
        Page<EquipmentReservationVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<EquipmentReservationVo> voList = page.getRecords().stream().map(reservation -> {
            EquipmentReservationVo vo = new EquipmentReservationVo();
            BeanUtils.copyProperties(reservation, vo);
            // 填充器材名称
            Equipment equipment = this.getById(reservation.getEquipmentId());
            if (equipment != null) {
                vo.setEquipmentName(equipment.getName());
            }
            // 填充学员姓名
            StudentVo student = studentService.getStudentById(reservation.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getName());
            }
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 获取健身器材查询条件
     *
     * @param equipmentQueryPageRequest 查询条件
     * @return 查询条件
     */
    private LambdaQueryWrapper<Equipment> getEquipmentQueryWrapper(
            EquipmentQueryPageRequest equipmentQueryPageRequest) {
        LambdaQueryWrapper<Equipment> queryWrapper = new LambdaQueryWrapper<>();
        if (equipmentQueryPageRequest == null) {
            return queryWrapper;
        }

        String name = equipmentQueryPageRequest.getName();
        String location = equipmentQueryPageRequest.getLocation();
        Integer status = equipmentQueryPageRequest.getStatus();

        // 模糊查询器材名称
        queryWrapper.like(StringUtils.isNotBlank(name), Equipment::getName, name);
        // 模糊查询放置位置
        queryWrapper.like(StringUtils.isNotBlank(location), Equipment::getLocation, location);
        // 精确查询状态
        queryWrapper.eq(status != null, Equipment::getStatus, status);

        return queryWrapper;
    }
}

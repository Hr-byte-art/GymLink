package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.equipment.*;
import com.ldr.gymlink.model.entity.Equipment;
import com.ldr.gymlink.model.vo.EquipmentReservationVo;
import com.ldr.gymlink.model.vo.EquipmentVo;
public interface EquipmentService extends IService<Equipment> {

    /**
     * 添加健身器材
     *
     * @param addEquipmentRequest 添加健身器材请求
     * @return 健身器材信息
     */
    EquipmentVo addEquipment(AddEquipmentRequest addEquipmentRequest);

    /**
     * 获取所有健身器材信息(分页)
     *
     * @param equipmentQueryPageRequest 分页查询请求
     * @return 健身器材信息列表
     */
    Page<EquipmentVo> listEquipmentPage(EquipmentQueryPageRequest equipmentQueryPageRequest);

    /**
     * 删除健身器材
     *
     * @param id 健身器材id
     * @return 是否删除成功
     */
    boolean deleteEquipment(Long id);

    /**
     * 修改健身器材信息
     *
     * @param id                     健身器材id
     * @param updateEquipmentRequest 修改健身器材信息请求
     * @return 是否修改成功
     */
    boolean updateEquipment(Long id, UpdateEquipmentRequest updateEquipmentRequest);

    /**
     * 根据id获取健身器材信息
     *
     * @param id 健身器材id
     * @return 健身器材信息
     */
    EquipmentVo getEquipmentById(Long id);

    /**
     * 预约健身器材
     *
     * @param bookingEquipmentRequest 预约健身器材请求
     * @return 是否预约成功
     */
    boolean bookingEquipment(BookingEquipmentRequest bookingEquipmentRequest);

    /**
     * 取消预约健身器材
     *
     * @param bookingId 预约id
     * @return 是否取消成功
     */
    boolean cancelBookingEquipment(Long bookingId);

    /**
     * 获取学员预约记录(分页)
     *
     * @param studentReservationQueryRequest 查询请求
     * @return 预约记录列表
     */
    Page<EquipmentReservationVo> listStudentReservationPage(
            StudentReservationQueryRequest studentReservationQueryRequest);

    /**
     * 获取所有预约记录(分页)
     *
     * @param allReservationQueryRequest 查询请求
     * @return 预约记录列表
     */
    Page<EquipmentReservationVo> listAllReservationPage(AllReservationQueryRequest allReservationQueryRequest);

    /**
     * 获取某器材的预约记录(分页)
     *
     * @param request 查询请求
     * @return 预约记录列表
     */
    Page<EquipmentReservationVo> listReservationsByEquipment(EquipmentReservationSearchRequest request);

    /**
     * 获取某时间段内的预约记录(分页)
     *
     * @param request 查询请求
     * @return 预约记录列表
     */
    Page<EquipmentReservationVo> listReservationsByTimeRange(TimeRangeReservationSearchRequest request);

    /**
     * 获取某学员在某时间段内的预约记录(分页)
     *
     * @param request 查询请求
     * @return 预约记录列表
     */
    Page<EquipmentReservationVo> listStudentReservationsByTimeRange(StudentTimeReservationSearchRequest request);
}

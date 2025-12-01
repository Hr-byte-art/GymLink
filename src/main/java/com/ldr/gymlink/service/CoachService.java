package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.coach.AddCoachRequest;
import com.ldr.gymlink.model.dto.coach.CoachQueryPageRequest;
import com.ldr.gymlink.model.dto.coach.UpdateCoachRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.vo.CoachVo;

/**
 * @author 木子宸
 * @description 针对表【coach】的数据库操作Service
 * @createDate 2025-11-29 14:40:18
 */
public interface CoachService extends IService<Coach> {

    /**
     * 根据用户id获取教练信息
     *
     * @param userId 用户id
     * @return 教练信息
     */
    CoachVo getCoachByUserId(Long userId);

    /**
     * 添加教练
     *
     * @param addCoachRequest 添加教练请求
     * @return 教练信息
     */
    CoachVo addCoach(AddCoachRequest addCoachRequest);

    /**
     * 获取所有教练信息(分页)
     *
     * @return 教练信息列表
     */
    Page<CoachVo> listCoachPage(CoachQueryPageRequest coachQueryPageRequest);

    /**
     * 删除教练
     *
     * @param id 教练id
     * @return 是否删除成功
     */
    boolean deleteCoach(Long id);

    /**
     * 修改教练信息
     *
     * @param updateCoachRequest 修改教练信息请求
     * @return 是否修改成功
     */
    boolean updateCoach(Long id, UpdateCoachRequest updateCoachRequest);

    /**
     * 根据id获取教练信息
     *
     * @param id 教练id
     * @return 教练信息
     */
    CoachVo getCoachById(Long id);

    /**
     * 预约教练
     *
     * @param bookingCoachRequest 预约教练请求
     * @return 是否预约成功
     */
    boolean bookingCoach(com.ldr.gymlink.model.dto.coach.BookingCoachRequest bookingCoachRequest);

    /**
     * 取消教练预约
     *
     * @param appointmentId 预约id
     * @return 是否取消成功
     */
    boolean cancelCoachAppointment(Long appointmentId);

    /**
     * 确认教练预约(教练操作)
     *
     * @param appointmentId 预约id
     * @return 是否确认成功
     */
    boolean confirmCoachAppointment(Long appointmentId);

    /**
     * 拒绝教练预约(教练操作)
     *
     * @param appointmentId 预约id
     * @return 是否拒绝成功
     */
    boolean rejectCoachAppointment(Long appointmentId);

    /**
     * 获取学员的教练预约记录(分页)
     *
     * @param request 查询请求
     * @return 预约记录列表
     */
    Page<com.ldr.gymlink.model.vo.CoachAppointmentVo> listStudentCoachAppointmentPage(
            com.ldr.gymlink.model.dto.coach.StudentCoachAppointmentQueryRequest request);

    /**
     * 获取所有教练预约记录(分页)
     *
     * @param request 查询请求
     * @return 预约记录列表
     */
    Page<com.ldr.gymlink.model.vo.CoachAppointmentVo> listAllCoachAppointmentPage(
            com.ldr.gymlink.model.dto.coach.AllCoachAppointmentQueryRequest request);

    /**
     * 获取某教练的预约记录(分页)
     *
     * @param request 查询请求
     * @return 预约记录列表
     */
    Page<com.ldr.gymlink.model.vo.CoachAppointmentVo> listAppointmentsByCoach(
            com.ldr.gymlink.model.dto.coach.CoachAppointmentSearchRequest request);

    /**
     * 获取某时间段内的教练预约记录(分页)
     *
     * @param request 查询请求
     * @return 预约记录列表
     */
    Page<com.ldr.gymlink.model.vo.CoachAppointmentVo> listAppointmentsByTimeRange(
            com.ldr.gymlink.model.dto.coach.CoachTimeAppointmentSearchRequest request);

    /**
     * 获取某学员在某时间段内的教练预约记录(分页)
     *
     * @param request 查询请求
     * @return 预约记录列表
     */
    Page<com.ldr.gymlink.model.vo.CoachAppointmentVo> listStudentAppointmentsByTimeRange(
            com.ldr.gymlink.model.dto.coach.StudentCoachTimeAppointmentSearchRequest request);
}

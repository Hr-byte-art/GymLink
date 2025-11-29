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
}

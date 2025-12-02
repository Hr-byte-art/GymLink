package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.model.entity.Experience;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.experience.AddExperienceRequest;
import com.ldr.gymlink.model.dto.experience.ExperienceQueryPageRequest;
import com.ldr.gymlink.model.dto.experience.UpdateExperienceRequest;
import com.ldr.gymlink.model.vo.ExperienceVo;

/**
 * @author 木子宸
 * @description 针对表【experience(健身经验/社区帖子表)】的数据库操作Service
 * @createDate 2025-12-02 20:39:04
 */
public interface ExperienceService extends IService<Experience> {

    /**
     * 发布健身经验
     *
     * @param addExperienceRequest 发布健身经验请求
     * @return 健身经验信息
     */
    ExperienceVo addExperience(AddExperienceRequest addExperienceRequest);

    /**
     * 获取所有健身经验(分页)
     *
     * @param experienceQueryPageRequest 分页查询请求
     * @return 健身经验列表
     */
    Page<ExperienceVo> listExperiencePage(ExperienceQueryPageRequest experienceQueryPageRequest);

    /**
     * 删除健身经验
     *
     * @param id 经验id
     * @return 是否删除成功
     */
    boolean deleteExperience(Long id);

    /**
     * 修改健身经验信息
     *
     * @param id                      经验id
     * @param updateExperienceRequest 修改经验信息请求
     * @return 是否修改成功
     */
    boolean updateExperience(Long id, UpdateExperienceRequest updateExperienceRequest);

    /**
     * 根据id获取健身经验详情（同时增加浏览量）
     *
     * @param id 经验id
     * @return 健身经验信息
     */
    ExperienceVo getExperienceById(Long id);
}

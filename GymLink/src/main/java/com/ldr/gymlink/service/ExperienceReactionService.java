package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.experience.GetUserReactionRequest;
import com.ldr.gymlink.model.dto.experience.UserCancelReactionRequest;
import com.ldr.gymlink.model.dto.experience.UserReactionRequest;
import com.ldr.gymlink.model.entity.ExperienceReaction;

/**
 * @author 木子宸
 * @description 针对表【experience_reaction(帖子点赞/点不喜欢记录表)】的数据库操作Service
 * @createDate 2025-12-02 21:38:34
 */
public interface ExperienceReactionService extends IService<ExperienceReaction> {

    /**
     * 添加用户对帖子的反应
     *
     * @param userReactionRequest 用户对帖子的反应请求参数
     * @return 是否成功
     */
    Boolean addReaction(UserReactionRequest userReactionRequest);

    /**
     * 取消用户对帖子的反应
     *
     * @param userCancelReactionRequest 用户取消对帖子的反应请求参数
     * @return 是否成功
     */
    Boolean cancelReaction(UserCancelReactionRequest userCancelReactionRequest);

    /**
     * 获取帖子点赞数
     *
     * @param experienceId 帖子id
     * @return 点赞数
     */
    Long getLikesCount(Long experienceId);

    /**
     * 获取用户对帖子的反应
     *
     * @param getUserReactionRequest 用户对帖子的反应请求参数
     * @return 用户对帖子的反应
     */
    Integer getUserReaction(GetUserReactionRequest getUserReactionRequest);

    /**
     * 获取用户对帖子的反应（不校验权限，内部调用）
     *
     * @param getUserReactionRequest 用户对帖子的反应请求参数
     * @return 用户对帖子的反应类型
     */
    Integer getUserReactionWithoutAuth(GetUserReactionRequest getUserReactionRequest);
}

package com.ldr.gymlink.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.mapper.ExperienceReactionMapper;
import com.ldr.gymlink.model.dto.experience.GetUserReactionRequest;
import com.ldr.gymlink.model.dto.experience.UserCancelReactionRequest;
import com.ldr.gymlink.model.dto.experience.UserReactionRequest;
import com.ldr.gymlink.model.entity.ExperienceReaction;
import com.ldr.gymlink.model.enums.ExperienceReactionEnum;
import com.ldr.gymlink.service.ExperienceReactionService;
import com.ldr.gymlink.utils.ThrowUtils;
import org.springframework.stereotype.Service;

import java.util.Date;


/**
 * @author 木子宸
 * @description 针对表【experience_reaction(帖子点赞/点不喜欢记录表)】的数据库操作Service实现
 * @createDate 2025-12-02 21:38:34
 */
@Service
public class ExperienceReactionServiceImpl extends ServiceImpl<ExperienceReactionMapper, ExperienceReaction>
        implements ExperienceReactionService {

    @Override
    public Boolean addReaction(UserReactionRequest userReactionRequest) {
        // 1. 基础参数校验
        ThrowUtils.throwIf(userReactionRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        Long experienceId = userReactionRequest.getExperienceId();
        Integer reaction = userReactionRequest.getReaction();
        Long userId = userReactionRequest.getUserId();
        ThrowUtils.throwIf(experienceId == null, ErrorCode.PARAMS_ERROR, "帖子id不能为空");
        ThrowUtils.throwIf(reaction == null, ErrorCode.PARAMS_ERROR, "反应不能为空");
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户id不能为空");
        // 在执行业务前增加用户身份验证
        Long currentUserId = StpUtil.getLoginIdAsLong();
        ThrowUtils.throwIf(!currentUserId.equals(userId), ErrorCode.NO_AUTH_ERROR, "无权限操作");


        // 2. 枚举值校验
        ExperienceReactionEnum reactionEnum;
        try {
            reactionEnum = ExperienceReactionEnum.getByValue(reaction);
        } catch (IllegalArgumentException e) {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, e.getMessage());
            return false;
        }

        // 3. 查询是否已存在记录
        LambdaQueryWrapper<ExperienceReaction> queryWrapper = new LambdaQueryWrapper<ExperienceReaction>()
                .eq(ExperienceReaction::getExperienceId, experienceId)
                .eq(ExperienceReaction::getUserId, userId);
        ExperienceReaction existingReaction = this.getOne(queryWrapper);

        if (existingReaction != null) {
            // 已存在，更新反应类型
            existingReaction.setReactionType(reactionEnum.getValue());
            existingReaction.setUpdatedAt(new Date());
            return this.updateById(existingReaction);
        } else {
            // 不存在，新增记录
            ExperienceReaction experienceReaction = new ExperienceReaction();
            experienceReaction.setExperienceId(experienceId);
            experienceReaction.setUserId(userId);
            experienceReaction.setReactionType(reactionEnum.getValue());
            experienceReaction.setUpdatedAt(new Date());
            return this.save(experienceReaction);
        }
    }


    @Override
    public Boolean cancelReaction(UserCancelReactionRequest userCancelReactionRequest) {
        Long experienceId = userCancelReactionRequest.getExperienceId();
        Long userId = userCancelReactionRequest.getUserId();

        ThrowUtils.throwIf(experienceId == null, ErrorCode.PARAMS_ERROR, "帖子id不能为空");
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户id不能为空");
        // 在执行业务前增加用户身份验证
        Long currentUserId = StpUtil.getLoginIdAsLong();
        ThrowUtils.throwIf(!currentUserId.equals(userId), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        LambdaQueryWrapper<ExperienceReaction> queryWrapper = new LambdaQueryWrapper<ExperienceReaction>().eq(ExperienceReaction::getExperienceId, experienceId)
                .eq(ExperienceReaction::getUserId, userId);
        return this.remove(queryWrapper);
    }

    @Override
    public Long getLikesCount(Long experienceId) {
        LambdaQueryWrapper<ExperienceReaction> queryWrapper = new LambdaQueryWrapper<ExperienceReaction>().eq(ExperienceReaction::getExperienceId, experienceId)
                .eq(ExperienceReaction::getReactionType, ExperienceReactionEnum.LIKE.getValue());
        long count = this.count(queryWrapper);
        return count;
    }

    @Override
    public Integer getUserReaction(GetUserReactionRequest getUserReactionRequest) {
        Long experienceId = getUserReactionRequest.getExperienceId();
        Long userId = getUserReactionRequest.getUserId();
        ThrowUtils.throwIf(experienceId == null, ErrorCode.PARAMS_ERROR, "帖子id不能为空");
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户id不能为空");
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        ThrowUtils.throwIf(loginIdAsLong != userId, ErrorCode.NO_AUTH_ERROR, "无权限操作");

        LambdaQueryWrapper<ExperienceReaction> queryWrapper = new LambdaQueryWrapper<ExperienceReaction>().eq(ExperienceReaction::getExperienceId, experienceId)
                .eq(ExperienceReaction::getUserId, userId);
        ExperienceReaction experienceReaction = this.getOne(queryWrapper);
        return experienceReaction == null ? null : experienceReaction.getReactionType();
    }

    @Override
    public Integer getUserReactionWithoutAuth(GetUserReactionRequest getUserReactionRequest) {
        Long experienceId = getUserReactionRequest.getExperienceId();
        Long userId = getUserReactionRequest.getUserId();
        if (experienceId == null || userId == null) {
            return null;
        }

        LambdaQueryWrapper<ExperienceReaction> queryWrapper = new LambdaQueryWrapper<ExperienceReaction>()
                .eq(ExperienceReaction::getExperienceId, experienceId)
                .eq(ExperienceReaction::getUserId, userId);
        ExperienceReaction experienceReaction = this.getOne(queryWrapper);
        return experienceReaction == null ? null : experienceReaction.getReactionType();
    }
}





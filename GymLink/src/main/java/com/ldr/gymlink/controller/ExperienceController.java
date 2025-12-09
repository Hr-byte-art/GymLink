package com.ldr.gymlink.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.experience.*;
import com.ldr.gymlink.model.vo.ExperienceVo;
import com.ldr.gymlink.service.ExperienceReactionService;
import com.ldr.gymlink.service.ExperienceService;
import com.ldr.gymlink.utils.ResultUtils;
import com.ldr.gymlink.utils.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 21:00:00
 * @Description 健身经验/社区管理
 */
@RestController
@RequestMapping("/experience")
@Tag(name = "健身经验/社区管理")
public class ExperienceController {

    @Resource
    private ExperienceService experienceService;

    @Resource
    private ExperienceReactionService experienceReactionService;

    @PostMapping("/addExperience")
    @Operation(summary = "发布健身经验")
    public BaseResponse<ExperienceVo> addExperience(@RequestBody AddExperienceRequest addExperienceRequest) {
        ExperienceVo experienceVo = experienceService.addExperience(addExperienceRequest);
        return ResultUtils.success(experienceVo);
    }

    @PostMapping("/listExperience")
    @Operation(summary = "分页查询健身经验")
    public BaseResponse<Page<ExperienceVo>> listExperience(
            @RequestBody ExperienceQueryPageRequest experienceQueryPageRequest) {
        Page<ExperienceVo> experienceVos = experienceService.listExperiencePage(experienceQueryPageRequest);
        return ResultUtils.success(experienceVos);
    }

    @PostMapping("/deleteExperience")
    @Operation(summary = "删除健身经验")
    public BaseResponse<Boolean> deleteExperience(@RequestParam Long id) {
        boolean delete = experienceService.deleteExperience(id);
        return ResultUtils.success(delete);
    }

    @PostMapping("/updateExperience")
    @Operation(summary = "更新健身经验")
    public BaseResponse<Boolean> updateExperience(
            @RequestParam Long id,
            @RequestBody UpdateExperienceRequest updateExperienceRequest) {
        boolean update = experienceService.updateExperience(id, updateExperienceRequest);
        return ResultUtils.success(update);
    }

    @GetMapping("/getExperienceById")
    @Operation(summary = "获取健身经验详情（同时增加浏览量）")
    public BaseResponse<ExperienceVo> getExperienceById(@RequestParam Long id) {
        ExperienceVo experienceVo = experienceService.getExperienceById(id);
        return ResultUtils.success(experienceVo);
    }


    @PostMapping("/userReaction")
    @Operation(summary = "用户对帖子进行反应")
    public BaseResponse<Boolean> addReaction(@RequestBody UserReactionRequest userReactionRequest) {

        long loginUserId = StpUtil.getLoginIdAsLong();
        ThrowUtils.throwIf(loginUserId != userReactionRequest.getUserId(), ErrorCode.OPERATION_ERROR, "用户id不一致");
        Long experienceId = userReactionRequest.getExperienceId();
        Integer reaction = userReactionRequest.getReaction();
        Long userId = userReactionRequest.getUserId();
        ThrowUtils.throwIf(experienceId == null, ErrorCode.PARAMS_ERROR, "帖子id不能为空");
        ThrowUtils.throwIf(reaction == null, ErrorCode.PARAMS_ERROR, "反应不能为空");
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户id不能为空");
        Boolean addReaction = experienceReactionService.addReaction(userReactionRequest);
        return ResultUtils.success(addReaction);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消用户对帖子的反应")
    public BaseResponse<Boolean> cancelReaction(@RequestBody UserCancelReactionRequest userCancelReactionRequest) {
        Long experienceId = userCancelReactionRequest.getExperienceId();
        Long userId = userCancelReactionRequest.getUserId();
        ThrowUtils.throwIf(experienceId == null, ErrorCode.PARAMS_ERROR, "帖子id不能为空");
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户id不能为空");
        Boolean cancelReaction = experienceReactionService.cancelReaction(userCancelReactionRequest);
        return ResultUtils.success(cancelReaction);
    }

    /**
     * 获取帖子的反应统计（点赞数+不喜欢数）
     */
    @GetMapping("/getLikesCount")
    public BaseResponse<Long> getLikesCount(@RequestParam Long experienceId) {
        ThrowUtils.throwIf(experienceId == null, ErrorCode.PARAMS_ERROR, "帖子id不能为空");
        Long likesCount = experienceReactionService.getLikesCount(experienceId);
        return ResultUtils.success(likesCount);
    }

    /**
     * 获取当前用户对帖子的反应
     */
    @GetMapping("/getCurrentUserReaction")
    public BaseResponse<Integer> getUserReaction(@RequestParam Long experienceId, @RequestParam Long userId) {
        ThrowUtils.throwIf(experienceId == null, ErrorCode.PARAMS_ERROR, "帖子id不能为空");
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户id不能为空");
        GetUserReactionRequest getUserReactionRequest = new GetUserReactionRequest();
        getUserReactionRequest.setExperienceId(experienceId);
        getUserReactionRequest.setUserId(userId);
        Integer userReaction = experienceReactionService.getUserReaction(getUserReactionRequest);
        return ResultUtils.success(userReaction);
    }
}

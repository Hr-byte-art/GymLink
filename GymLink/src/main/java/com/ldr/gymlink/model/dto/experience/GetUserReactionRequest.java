package com.ldr.gymlink.model.dto.experience;

import lombok.Data;

/**
 * @Author 王哈哈
 * @Date 2025/12/2 22:37:23
 * @Description 获取用户对帖子的反应
 */
@Data
public class GetUserReactionRequest {
    /**
     * 帖子id
     */
    private Long experienceId;

    /**
     * 用户id
     */
    private Long userId;
}

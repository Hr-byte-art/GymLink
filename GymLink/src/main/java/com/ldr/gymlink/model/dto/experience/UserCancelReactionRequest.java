package com.ldr.gymlink.model.dto.experience;

import lombok.Data;

/**
 * @Author 王哈哈
 * @Date 2025/12/2 21:54:59
 * @Description 添加用户对健身经验反应的请求参数
 */
@Data
public class UserCancelReactionRequest {
    /**
     * 健身经验id
     */
    private Long experienceId;

    /**
     * 用户id
     */
    private Long userId;
}

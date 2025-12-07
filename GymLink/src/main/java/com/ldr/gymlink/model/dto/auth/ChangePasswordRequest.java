package com.ldr.gymlink.model.dto.auth;

import lombok.Data;

/**
 * @Author 王哈哈
 * @Date 2025/12/6 22:35:50
 * @Description 修改密码请求
 */
@Data
public class ChangePasswordRequest {

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkedPassword;
}

package com.ldr.gymlink.utils;

import cn.dev33.satoken.stp.StpUtil;

/**
 * @Author 王哈哈
 * @Date 2025/12/2 22:19:45
 * @Description 验证登录用户
 */
public class VerifyLoginUsersUtils {
    public static boolean verifyLoginUsers(Long userId) {
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        return loginIdAsLong == userId;
    }
}

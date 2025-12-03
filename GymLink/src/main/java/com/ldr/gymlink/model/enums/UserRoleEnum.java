package com.ldr.gymlink.model.enums;

import lombok.Getter;

/**
 * @Author 王哈哈
 * @Date 2025/11/29 15:34:26
 * @Description 用户角色枚举
 */
@Getter
public enum UserRoleEnum {
    STUDENT("学员", "student"),
    ADMIN("管理员", "admin"),
    COACH("教练", "coach");

    private String role;
    private String value;

    UserRoleEnum(String role, String value) {
        this.role = role;
        this.value = value;
    }

    /**
     * 根据角色获取枚举
     *
     * @param role 角色
     * @return 枚举
     */

    public static UserRoleEnum getEnumByRole(String role) {
        //判空
        if (role == null) {
            return null;
        }

        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.getRole().equals(role)) {
                return userRoleEnum;
            }
        }
        return null;
    }

    /**
     * 根据枚举值获取枚举
     *
     * @param value 枚举值
     * @return 枚举
     */

    public static UserRoleEnum getEnumByValue(String value) {
        //判空
        if (value == null) {
            return null;
        }

        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.getValue().equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}

package com.ldr.gymlink.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @Author 王哈哈
 * @Date 2025/11/29 14:32:15
 * @Description 权限校验
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有该角色
     *
     * @return
     */
    String mustRole() default "";
}

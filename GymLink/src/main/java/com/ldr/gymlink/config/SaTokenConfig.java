package com.ldr.gymlink.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类 - 前后端分离模式
 * 
 * @author GymLink
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    
    /**
     * 注册 Sa-Token 拦截器
     * 在前后端分离模式下，Sa-Token 会自动从请求头中读取 token
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 这里可以添加全局的登录校验逻辑
            // 但由于我们使用了 @AuthCheck 注解，这里暂时不做全局校验
        }))
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/user/login",           // 登录接口
            "/user/register",        // 注册接口
            "/error",                // 错误页面
            "/doc.html",             // Knife4j 文档
            "/swagger-resources/**", // Swagger 资源
            "/webjars/**",           // Swagger UI 资源
            "/v3/**"                 // OpenAPI 3.0 资源
        );
    }
}

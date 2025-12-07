package com.ldr.gymlink.aop;

import com.ldr.gymlink.annotation.AuthCheck;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.enums.UserRoleEnum;
import com.ldr.gymlink.model.vo.UserVo;
import com.ldr.gymlink.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Author 王哈哈
 * @Date 2025/11/29 14:34:17
 * @Description 权限身份验证拦截器
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        UserRoleEnum mustUserRole = UserRoleEnum.getEnumByValue(mustRole);
        // 无需权限，直接放行
        if (mustUserRole == null) {
            return joinPoint.proceed();
        }

        //需要权限 - 判断当前用户身份是否是管理员
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取当前用户
        UserVo currentUserRole = userService.getLoginUser();
        // 获取当前用户的角色
        String role = currentUserRole.getRole();
        // 检查是否有权限
        // 无权限拒绝放行
        if (!role.equals(mustRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (mustUserRole.equals(UserRoleEnum.ADMIN) && !role.equals(UserRoleEnum.ADMIN.getValue())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 校验通过，放行
        return joinPoint.proceed();
    }
}

package com.ldr.gymlink.controller;

import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.model.vo.UserVo;
import com.ldr.gymlink.service.UserService;
import com.ldr.gymlink.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author 王哈哈
 * @Date 2025/11/27 23:21:59
 * @Description 用户控制类
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户控制类", description = "用户控制类")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public BaseResponse<UserVo> login(String userAccount, String password, String role, Boolean rememberMe) {
        UserVo login = userService.userLogin(userAccount, password, role, rememberMe);
        return ResultUtils.success(login);
    }

    @PostMapping("/userRegister")
    @Operation(summary = "用户注册")
    public BaseResponse<Long> userRegister(String userAccount, String userPassword, String checkPassword) {
        long register = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(register);
    }

    @PostMapping("/userLogout")
    @Operation(summary = "用户注销")
    public void userLogout() {
        userService.userLogout();
    }

}

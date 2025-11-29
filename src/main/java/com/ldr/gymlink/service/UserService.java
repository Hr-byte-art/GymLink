package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author 木子宸
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2025-11-27 23:19:12
 */
public interface UserService extends IService<User> {

    /**
     * 获取当前登录用户
     * 
     * @param request 请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户登录
     * 
     * @param userAccount 账号
     * @param password    密码
     * @param role        角色
     * @param rememberMe  记住我
     * @return 登录成功返回用户信息
     */
    UserVo userLogin(String userAccount, String password, String role, Boolean rememberMe);

    /**
     * 用户注册
     * 
     * @param userAccount   账号
     * @param userPassword  密码
     * @param checkPassword 密码
     * @return 注册成功返回用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户注销
     * 
     * @return 注销成功返回0
     */
    void userLogout();
}

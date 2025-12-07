package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.auth.ChangePasswordRequest;
import com.ldr.gymlink.model.dto.coach.CoachQueryPageRequest;
import com.ldr.gymlink.model.dto.student.StudentQueryPageRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.Student;
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
     * @return 当前登录用户
     */
    UserVo getLoginUser();

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

    /**
     * 密码加密
     *
     * @param userPassword 密码
     * @return 加密后的密码
     */
    String encryptPassword(String userPassword);

    /**
     * 获取查询条件
     *
     * @param studentQueryPageRequest 查询条件
     * @return 查询条件
     */
    LambdaQueryWrapper<Student> getQueryWrapper(StudentQueryPageRequest studentQueryPageRequest);

    /**
     * 获取教练查询条件
     *
     * @param coachQueryPageRequest 查询条件
     * @return 查询条件
     */
    LambdaQueryWrapper<Coach> getCoachQueryWrapper(CoachQueryPageRequest coachQueryPageRequest);

    /**
     * 修改密码
     *
     * @param changePasswordRequest 修改密码请求
     * @return 修改成功返回true
     */
    Boolean changePassword(ChangePasswordRequest changePasswordRequest);
}

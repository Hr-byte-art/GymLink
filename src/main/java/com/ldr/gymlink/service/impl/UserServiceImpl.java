package com.ldr.gymlink.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.mapper.UserMapper;
import com.ldr.gymlink.model.entity.Student;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.vo.UserVo;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static com.ldr.gymlink.constant.RoleConstant.DEFAULT_ROLE;
import static com.ldr.gymlink.constant.RoleConstant.USER_LOGIN_STATE;

/**
 * @author 木子宸
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-11-27 23:19:11
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    @Resource
    private StudentService studentService;

    private final String salt = "wang-haha";

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断是否登录
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        return null;
    }

    @Override
    public UserVo userLogin(String userAccount, String password, String role, Boolean rememberMe) {

        if (userAccount == null || password == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数错误，请检查后重试");
        }
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不小于6位，请检查的后重试");
        }

        // 获取加密后的密码
        String encryptedPassword = encryptPassword(password);

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getUsername, userAccount)
                .eq(User::getPassword, encryptedPassword);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在，请检查后重试");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已禁用，请检查后重试");
        }
        if (!user.getRole().equals(role)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户角色错误，请检查后重试");
        }

        // 根据 rememberMe 参数决定登录方式
        // true 或 null（默认）: 记住我模式，持久化 Cookie
        // false: 本次会话有效，临时 Cookie
        if (rememberMe != null && !rememberMe) {
            StpUtil.login(user.getId(), false);
        } else {
            StpUtil.login(user.getId());
        }

        StpUtil.getSession().set(USER_LOGIN_STATE, user);
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 校验参数
        if (userAccount == null || userPassword == null || checkPassword == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数错误，请检查后重试");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号长度不小于4位，请检查后重试");
        }
        if (userPassword.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码长度不小于6位，请检查后重试");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致，请检查后重试");
        }
        boolean exists = this.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, userAccount));
        if (exists) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号已存在，请检查后重试");
        }
        // 密码加密
        String encryptedPassword = encryptPassword(userPassword);
        // 插入数据
        User user = new User();
        user.setUsername(userAccount);
        user.setPassword(encryptedPassword);
        user.setStatus(1);
        user.setRole(DEFAULT_ROLE);
        user.setCreateTime(new Date());
        user.setIsDelete(0);

        // 新建学员
        Student student = new Student();
        student.setUsername(userAccount);
        student.setPassword(encryptedPassword);
        student.setName("用户"+RandomUtil.randomString(6));
        student.setGender(3);
        student.setCreateTime(new Date());
        boolean studentSave = studentService.save(student);
        Long studentId = student.getId();
        // 绑定学员
        user.setAssociatedUserId(studentId);

        boolean save = this.save(user);
        if (!save || !studentSave) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，请检查后重试");
        }
        return user.getId();
    }

    @Override
    public void userLogout() {
        boolean login = StpUtil.isLogin();
        if (!login) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录，请检查后重试");
        }
        StpUtil.logout();
    }

    // 密码加密
    private String encryptPassword(String userPassword) {
        // 加盐-混淆密码
        return DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
    }


}

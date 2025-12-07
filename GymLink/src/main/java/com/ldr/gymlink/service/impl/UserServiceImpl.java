package com.ldr.gymlink.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.mapper.CoachMapper;
import com.ldr.gymlink.mapper.StudentMapper;
import com.ldr.gymlink.mapper.UserMapper;
import com.ldr.gymlink.model.dto.auth.ChangePasswordRequest;
import com.ldr.gymlink.model.dto.coach.CoachQueryPageRequest;
import com.ldr.gymlink.model.dto.student.StudentQueryPageRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.Student;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.vo.UserVo;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.service.UserService;
import com.ldr.gymlink.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.util.Date;

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
    private StudentMapper studentMapper;

    @Resource
    private CoachMapper coachMapper;

    private final String salt = "wang-haha";

    @Override
    public UserVo getLoginUser() {
        // 判断是否登录
        if (!StpUtil.isLogin()) {
            return null;
        }
        // 使用 Sa-Token 的 Session 获取登录用户信息
        Object attribute = StpUtil.getSession().get(USER_LOGIN_STATE);
        if (attribute == null) {
            return null;
        }
        User user = (User) attribute;
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        
        // 根据角色查询并填充详细信息（name、avatar 等）
        try {
            String role = user.getRole();
            if ("coach".equals(role)) {
                // 查询教练详细信息
                LambdaQueryWrapper<Coach> queryWrapper = new LambdaQueryWrapper<Coach>()
                        .eq(Coach::getId, user.getAssociatedUserId());
                Coach coach = coachMapper.selectOne(queryWrapper);
                if (coach != null) {
                    userVo.setName(coach.getName());
                    userVo.setAvatar(coach.getAvatar());
                }
            } else if ("student".equals(role)) {
                // 查询学员详细信息
                LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<Student>()
                        .eq(Student::getId, user.getAssociatedUserId());
                Student student = studentMapper.selectOne(queryWrapper);
                if (student != null) {
                    userVo.setName(student.getName());
                    userVo.setAvatar(student.getAvatar());
                }
            }
        } catch (Exception e) {
            // 如果查询详细信息失败，仍然返回基本用户信息
            // 不影响登录状态
        }
        
        return userVo;
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
        
        // 获取 token 并返回给前端
        String tokenValue = StpUtil.getTokenValue();
        
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        userVo.setToken(tokenValue);
        
        // 根据角色查询并填充详细信息（name、avatar 等）
        try {
            if ("coach".equals(role)) {
                // 查询教练详细信息
                LambdaQueryWrapper<Coach> coachQueryWrapper = new LambdaQueryWrapper<Coach>()
                        .eq(Coach::getId, user.getAssociatedUserId());
                Coach coach = coachMapper.selectOne(coachQueryWrapper);
                if (coach != null) {
                    userVo.setName(coach.getName());
                    userVo.setAvatar(coach.getAvatar());
                }
            } else if ("student".equals(role)) {
                // 查询学员详细信息
                LambdaQueryWrapper<Student> studentQueryWrapper = new LambdaQueryWrapper<Student>()
                        .eq(Student::getId, user.getAssociatedUserId());
                Student student = studentMapper.selectOne(studentQueryWrapper);
                if (student != null) {
                    userVo.setName(student.getName());
                    userVo.setAvatar(student.getAvatar());
                }
            }
        } catch (Exception e) {
            // 如果查询详细信息失败，仍然返回基本用户信息
            // 不影响登录
        }
        
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
        student.setName("用户" + RandomUtil.randomString(6));
        student.setGender(3);
        student.setCreateTime(new Date());
        int insert = studentMapper.insert(student);
        Long studentId = student.getId();
        // 绑定学员
        user.setAssociatedUserId(studentId);

        boolean save = this.save(user);
        if (!save || insert <= 0) {
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
    @Override
    public String encryptPassword(String userPassword) {
        // 加盐-混淆密码
        return DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
    }

    @Override
    public LambdaQueryWrapper<Student> getQueryWrapper(StudentQueryPageRequest studentQueryPageRequest) {
        if (studentQueryPageRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String name = studentQueryPageRequest.getName();
        Integer gender = studentQueryPageRequest.getGender();
        String phone = studentQueryPageRequest.getPhone();
        BigDecimal minHeight = studentQueryPageRequest.getMinHeight();
        BigDecimal maxHeight = studentQueryPageRequest.getMaxHeight();
        BigDecimal minWeight = studentQueryPageRequest.getMinWeight();
        BigDecimal maxWeight = studentQueryPageRequest.getMaxWeight();
        Date createTime = studentQueryPageRequest.getCreateTime();
        String sortField = studentQueryPageRequest.getSortField();
        String sortOrder = studentQueryPageRequest.getSortOrder();

        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<Student>()
                .like(StrUtil.isNotBlank(name), Student::getName, name)
                .eq(gender != null, Student::getGender, gender)
                .like(StrUtil.isNotBlank(phone), Student::getPhone, phone)
                .ge(minHeight != null, Student::getHeight, minHeight)
                .le(maxHeight != null, Student::getHeight, maxHeight)
                .ge(minWeight != null, Student::getWeight, minWeight)
                .le(maxWeight != null, Student::getWeight, maxWeight)
                .ge(createTime != null, Student::getCreateTime, createTime);

        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = !"descend".equals(sortOrder);

            switch (sortField) {
                case "createTime" -> queryWrapper.orderBy(true, isAsc, Student::getCreateTime);
                case "name" -> queryWrapper.orderBy(true, isAsc, Student::getName);
                case "height" -> queryWrapper.orderBy(true, isAsc, Student::getHeight);
                case "weight" -> queryWrapper.orderBy(true, isAsc, Student::getWeight);
            }
        }
        return queryWrapper;
    }

    @Override
    public LambdaQueryWrapper<Coach> getCoachQueryWrapper(CoachQueryPageRequest coachQueryPageRequest) {
        if (coachQueryPageRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String name = coachQueryPageRequest.getName();
        Integer gender = coachQueryPageRequest.getGender();
        String phone = coachQueryPageRequest.getPhone();
        Integer minAge = coachQueryPageRequest.getMinAge();
        Integer maxAge = coachQueryPageRequest.getMaxAge();
        String specialty = coachQueryPageRequest.getSpecialty();
        Date createTime = coachQueryPageRequest.getCreateTime();
        String sortField = coachQueryPageRequest.getSortField();
        String sortOrder = coachQueryPageRequest.getSortOrder();

        LambdaQueryWrapper<Coach> queryWrapper = new LambdaQueryWrapper<Coach>()
                .like(StrUtil.isNotBlank(name), Coach::getName, name)
                .eq(gender != null, Coach::getGender, gender)
                .like(StrUtil.isNotBlank(phone), Coach::getPhone, phone)
                .ge(minAge != null, Coach::getAge, minAge)
                .le(maxAge != null, Coach::getAge, maxAge)
                .like(StrUtil.isNotBlank(specialty), Coach::getSpecialty, specialty)
                .ge(createTime != null, Coach::getCreateTime, createTime);
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = !"descend".equals(sortOrder);

            switch (sortField) {
                case "createTime" -> queryWrapper.orderBy(true, isAsc, Coach::getCreateTime);
                case "name" -> queryWrapper.orderBy(true, isAsc, Coach::getName);
                case "age" -> queryWrapper.orderBy(true, isAsc, Coach::getAge);
                case "specialty" -> queryWrapper.orderBy(true, isAsc, Coach::getSpecialty);
            }
        }
        return queryWrapper;
    }

    @Override
    public Boolean changePassword(ChangePasswordRequest changePasswordRequest) {
        StpUtil.checkLogin();
        Object object = StpUtil.getSession().get(USER_LOGIN_STATE);
        User loginUser = (User) object;
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getId, loginUser.getId());
        User user = this.getOne(queryWrapper);
        if (user ==  null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // 获取用户的密码
        String oldPassword = user.getPassword();
        String oldPasswordEncrypted = encryptPassword(changePasswordRequest.getOldPassword());
        String newPassword = changePasswordRequest.getNewPassword();
        ThrowUtils.throwIf(!oldPasswordEncrypted.equals(oldPassword), ErrorCode.OPERATION_ERROR, "原密码错误");
        String newPasswordEncrypted = encryptPassword(newPassword);
        ThrowUtils.throwIf(newPasswordEncrypted.equals(oldPassword), ErrorCode.OPERATION_ERROR, "新密码不能与原密码相同");
        user.setPassword(newPasswordEncrypted);
        return this.update(user, queryWrapper);
    }
}

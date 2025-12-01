package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.mapper.CourseMapper;
import com.ldr.gymlink.mapper.StudentMapper;
import com.ldr.gymlink.model.dto.student.AddStudentRequest;
import com.ldr.gymlink.model.dto.student.StudentQueryPageRequest;
import com.ldr.gymlink.model.dto.student.UpdateStudentRequest;
import com.ldr.gymlink.model.entity.*;
import com.ldr.gymlink.model.enums.UserRoleEnum;
import com.ldr.gymlink.model.vo.StudentVo;
import com.ldr.gymlink.service.*;
import com.ldr.gymlink.utils.ThrowUtils;
import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author 木子宸
 * @description 针对表【student(学员/用户信息表)】的数据库操作Service实现
 * @createDate 2025-11-29 20:39:16
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student>
        implements StudentService {

    @Resource
    private UserService userService;

    @Resource
    private RechargeRecordService rechargeRecordService;

    @Resource
    private CourseOrderService courseOrderService;

    @Resource
    private CourseService courseService;
    @Override
    public StudentVo getStudentByUserId(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户id不能为空");
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        String role = user.getRole();
        StudentVo studentVo = new StudentVo();
        if (UserRoleEnum.STUDENT.getValue().equals(role)) {
            LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<Student>()
                    .eq(Student::getId, user.getAssociatedUserId());
            Student student = this.getOne(queryWrapper);
            ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员信息不存在");
            BeanUtils.copyProperties(student, studentVo);
        }
        return studentVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentVo addStudent(AddStudentRequest addStudentRequest) {
        String password = addStudentRequest.getPassword();
        String string = userService.encryptPassword(password);
        addStudentRequest.setPassword(string);
        Student student = new Student();
        BeanUtils.copyProperties(addStudentRequest, student);
        student.setCreateTime(new Date());
        boolean save = this.save(student);

        User user = new User();
        user.setUsername(addStudentRequest.getUsername());
        user.setPassword(addStudentRequest.getPassword());
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setIsDelete(0);
        user.setRole(UserRoleEnum.STUDENT.getValue());
        user.setAssociatedUserId(student.getId());

        boolean save1 = userService.save(user);
        if (!save || !save1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败，请稍后重试");
        }
        StudentVo studentVo = new StudentVo();
        BeanUtils.copyProperties(student, studentVo);
        return studentVo;
    }

    @Override
    public Page<StudentVo> listStudentPage(StudentQueryPageRequest studentQueryPageRequest) {
        ThrowUtils.throwIf(studentQueryPageRequest == null, ErrorCode.PARAMS_ERROR , "请求参数为空");
        int pageSize = studentQueryPageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        int pageNum = studentQueryPageRequest.getPageNum();
        LambdaQueryWrapper<Student> queryWrapper = userService.getQueryWrapper(studentQueryPageRequest);
        Page<Student> page = this.page(Page.of(pageNum, pageSize), queryWrapper);

        Page<StudentVo> studentVoPage = new Page<>(pageNum, pageSize);
        studentVoPage.setTotal(page.getTotal());
        studentVoPage.setRecords(page.getRecords().stream().map(student -> {
            StudentVo studentVo = new StudentVo();
            BeanUtils.copyProperties(student, studentVo);
            return studentVo;
        }).toList());
        return studentVoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteStudent(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "学员id不能为空");
        Student student = this.getById(id);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");

        // 级联删除：删除关联的 User 记录
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getAssociatedUserId, id)
                .eq(User::getRole, UserRoleEnum.STUDENT.getValue());
        userService.remove(userQueryWrapper);

        // 删除学员记录
        return this.removeById(id);
    }

    @Override
    public boolean updateStudent(Long id,UpdateStudentRequest updateStudentRequest) {
        Student student = this.getById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        BeanUtils.copyProperties(updateStudentRequest, student);
        return this.updateById(student);
    }

    @Override
    public StudentVo getStudentById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "学员id不能为空");
        Student student = this.getById(id);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");
        StudentVo studentVo = new StudentVo();
        BeanUtils.copyProperties(student, studentVo);
        return studentVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean studentTopUp(Long id, BigDecimal money) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "学员id不能为空");
        ThrowUtils.throwIf(money == null, ErrorCode.PARAMS_ERROR, "充值金额不能为空");
        Student student = this.getById(id);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");
        student.setBalance(student.getBalance().add(money));
        boolean b = this.updateById(student);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "充值失败，请稍后重试");
        }
        RechargeRecord rechargeRecord = new RechargeRecord();
        rechargeRecord.setStudentId(id);
        rechargeRecord.setAmount(money);
        rechargeRecord.setCreateTime(new Date());
        boolean save = rechargeRecordService.save(rechargeRecord);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "充值记录保存失败，请稍后重试");
        }
        return true;
    }

    @Override
    public boolean studentsPurchaseCourses(Long studentId, Long courseId) {
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员id不能为空");
        ThrowUtils.throwIf(courseId == null, ErrorCode.PARAMS_ERROR, "课程id不能为空");
        Student student = this.getById(studentId);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");
        Course course = courseService.getById(courseId);
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        // 检查余额是否组狗
        BigDecimal balance = student.getBalance();
        if (balance.compareTo(course.getPrice()) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "余额不足");
        }
        student.setBalance(balance.subtract(course.getPrice()));
        boolean b = this.updateById(student);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "购买课程失败，请稍后重试");
        }
        CourseOrder courseOrder = new CourseOrder();
        courseOrder.setOrderNo(generateOrderNo(studentId));
        courseOrder.setStudentId(studentId);
        courseOrder.setCourseId(courseId);
        courseOrder.setCoachId(course.getCoachId());
        courseOrder.setPrice(course.getPrice());
        courseOrder.setStatus(1);
        courseOrder.setCreateTime(new Date());
        boolean save = courseOrderService.save(courseOrder);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "课程订单保存失败，请稍后重试");
        }
        return true;
    }

    // 计算订单号bound
    private String generateOrderNo(Long studentId) {
        return "ORDER_" + studentId + "_" + System.currentTimeMillis() ;
    }
}
package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.student.AddStudentRequest;
import com.ldr.gymlink.model.dto.student.StudentQueryPageRequest;
import com.ldr.gymlink.model.dto.student.UpdateStudentRequest;
import com.ldr.gymlink.model.entity.Student;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.enums.UserRoleEnum;
import com.ldr.gymlink.model.vo.StudentVo;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.mapper.StudentMapper;
import com.ldr.gymlink.service.UserService;
import com.ldr.gymlink.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

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

    @Override
    public StudentVo getStudentByUserId(Long userId) {
        User user = userService.getById(userId);
        String role = user.getRole();
        StudentVo studentVo = new StudentVo();
        if (UserRoleEnum.STUDENT.getValue().equals(role)) {
            LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<Student>()
                    .eq(Student::getId, user.getAssociatedUserId());
            Student student = this.getOne(queryWrapper);
            BeanUtils.copyProperties(student, studentVo);
        }
        return studentVo;
    }

    @Override
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
    public boolean deleteStudent(Long id) {
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
        Student student = this.getById(id);
        StudentVo studentVo = new StudentVo();
        BeanUtils.copyProperties(student, studentVo);
        return studentVo;
    }
}

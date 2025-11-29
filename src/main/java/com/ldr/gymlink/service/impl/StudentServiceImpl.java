package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.model.entity.Student;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.enums.UserRoleEnum;
import com.ldr.gymlink.model.vo.StudentVo;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.mapper.StudentMapper;
import com.ldr.gymlink.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author 木子宸
* @description 针对表【student(学员/用户信息表)】的数据库操作Service实现
* @createDate 2025-11-29 20:39:16
*/
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student>
    implements StudentService{

    @Resource
    private UserService userService;

    @Override
    public StudentVo getStudentByUserId(Long userId) {
        User user = userService.getById(userId);
        String role = user.getRole();
        StudentVo studentVo = new StudentVo();
        if (UserRoleEnum.STUDENT.getValue().equals(role)) {
            LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<Student>()
                    .eq(Student::getId , user.getAssociatedUserId());
            Student student = this.getOne(queryWrapper);
            BeanUtils.copyProperties(student, studentVo);
        }
        return studentVo;
    }
}






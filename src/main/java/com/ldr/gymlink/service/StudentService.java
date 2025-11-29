package com.ldr.gymlink.service;

import com.ldr.gymlink.model.entity.Student;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.vo.StudentVo;

/**
* @author 木子宸
* @description 针对表【student(学员/用户信息表)】的数据库操作Service
* @createDate 2025-11-29 20:39:16
*/
public interface StudentService extends IService<Student> {

    /**
     * 根据用户id获取学员信息
     * @param userId
     * @return
     */
    StudentVo getStudentByUserId(Long userId);

    /**
     * 获取当前登录学员信息
     * @return
     */
}

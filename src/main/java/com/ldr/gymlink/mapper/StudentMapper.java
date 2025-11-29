package com.ldr.gymlink.mapper;

import com.ldr.gymlink.model.entity.Student;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 木子宸
* @description 针对表【student(学员/用户信息表)】的数据库操作Mapper
* @createDate 2025-11-29 20:39:16
* @Entity com.ldr.gymlink.model.entity.Student
*/
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

}





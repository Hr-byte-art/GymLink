package com.ldr.gymlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ldr.gymlink.model.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 木子宸
* @description 针对表【course(健身课程表)】的数据库操作Mapper
* @createDate 2025-11-30 20:57:06
* @Entity com.ldr.gymlink.model.entity.Course
*/
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

}





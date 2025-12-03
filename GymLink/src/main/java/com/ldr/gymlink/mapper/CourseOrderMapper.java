package com.ldr.gymlink.mapper;

import com.ldr.gymlink.model.entity.CourseOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 木子宸
* @description 针对表【course_order(课程购买订单表)】的数据库操作Mapper
* @createDate 2025-12-01 20:53:03
* @Entity com.ldr.gymlink.model.entity.CourseOrder
*/
@Mapper
public interface CourseOrderMapper extends BaseMapper<CourseOrder> {

}





package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.model.entity.CourseOrder;
import com.ldr.gymlink.service.CourseOrderService;
import com.ldr.gymlink.mapper.CourseOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author 木子宸
* @description 针对表【course_order(课程购买订单表)】的数据库操作Service实现
* @createDate 2025-12-01 20:53:03
*/
@Service
public class CourseOrderServiceImpl extends ServiceImpl<CourseOrderMapper, CourseOrder>
    implements CourseOrderService{

}





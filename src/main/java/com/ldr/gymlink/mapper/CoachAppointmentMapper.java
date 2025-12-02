package com.ldr.gymlink.mapper;

import com.ldr.gymlink.model.entity.CoachAppointment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 木子宸
* @description 针对表【coach_appointment(教练预约表)】的数据库操作Mapper
* @createDate 2025-12-01 21:53:24
* @Entity com.ldr.gymlink.model.entity.CoachAppointment
*/
@Mapper
public interface CoachAppointmentMapper extends BaseMapper<CoachAppointment> {

}





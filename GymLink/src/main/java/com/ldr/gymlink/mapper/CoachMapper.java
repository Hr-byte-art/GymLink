package com.ldr.gymlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ldr.gymlink.model.entity.Coach;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 木子宸
* @description 针对表【coach】的数据库操作Mapper
* @createDate 2025-11-29 14:40:18
* @Entity com.ldr.gymlink.model.entity.Coach
*/
@Mapper
public interface CoachMapper extends BaseMapper<Coach> {

}





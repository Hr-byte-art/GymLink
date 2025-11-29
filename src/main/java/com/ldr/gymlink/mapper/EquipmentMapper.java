package com.ldr.gymlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ldr.gymlink.model.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 木子宸
* @description 针对表【equipment(健身器材表)】的数据库操作Mapper
* @createDate 2025-11-30 21:30:07
* @Entity com.ldr.gymlink.model.entity.Equipment
*/
@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {

}





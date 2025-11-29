package com.ldr.gymlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ldr.gymlink.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 木子宸
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2025-11-27 23:19:12
* @Entity com.ldr.gymlink.model.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {
}





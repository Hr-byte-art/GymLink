package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.model.entity.Admin;
import com.ldr.gymlink.service.AdminService;
import com.ldr.gymlink.mapper.AdminMapper;
import org.springframework.stereotype.Service;

/**
* @author 木子宸
* @description 针对表【admin(管理员表)】的数据库操作Service实现
* @createDate 2025-11-29 20:39:16
*/
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
    implements AdminService{

}





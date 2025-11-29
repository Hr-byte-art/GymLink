package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.enums.UserRoleEnum;
import com.ldr.gymlink.service.CoachService;
import com.ldr.gymlink.mapper.CoachMapper;
import com.ldr.gymlink.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
* @author 木子宸
* @description 针对表【coach】的数据库操作Service实现
* @createDate 2025-11-29 14:40:18
*/
@Service
public class CoachServiceImpl extends ServiceImpl<CoachMapper, Coach>
    implements CoachService{

    @Resource
    private UserService userService;

    @Override
    public Coach getCoachByUserId(Long userId) {
        User user = userService.getById(userId);
        String role = user.getRole();
        if (!UserRoleEnum.COACH.getValue().equals(role)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR , "用户身份信息有误，请联系管理员查看");
        }
        Coach coach = this.getOne(new LambdaQueryWrapper<Coach>().eq(Coach::getId, user.getAssociatedUserId()));



    }
}





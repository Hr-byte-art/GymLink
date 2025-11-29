package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.entity.Coach;

/**
* @author 木子宸
* @description 针对表【coach】的数据库操作Service
* @createDate 2025-11-29 14:40:18
*/
public interface CoachService extends IService<Coach> {

    /**
     * 根据用户id获取教练信息
     * @param userId
     * @return
     */
    Coach getCoachByUserId(Long userId);
}

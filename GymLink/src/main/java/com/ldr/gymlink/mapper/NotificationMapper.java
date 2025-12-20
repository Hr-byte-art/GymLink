package com.ldr.gymlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ldr.gymlink.model.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知消息 Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}

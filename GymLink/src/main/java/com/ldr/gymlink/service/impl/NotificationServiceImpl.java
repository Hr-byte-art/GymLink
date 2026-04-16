package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.exception.ErrorCode;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.mapper.NotificationMapper;
import com.ldr.gymlink.model.entity.Notification;
import com.ldr.gymlink.service.NotificationService;
import com.ldr.gymlink.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 通知服务实现
 */
@Service
@Slf4j
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    public void createNotification(Long userId, Integer type, String title, String content, Long relatedId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "通知接收用户ID不能为空");
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notification.setCreateTime(new Date());
        boolean saved = this.save(notification);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "通知保存失败");
        log.info("通知已创建: userId={}, type={}, title={}, relatedId={}", userId, type, title, relatedId);
    }

    @Override
    public Page<Notification> listUserNotifications(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        return this.count(queryWrapper);
    }

    @Override
    public void markAsRead(Long notificationId) {
        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getId, notificationId)
                .set(Notification::getIsRead, 1);
        this.update(updateWrapper);
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1);
        this.update(updateWrapper);
    }
}

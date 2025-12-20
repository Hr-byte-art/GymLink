package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.entity.Notification;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 创建通知
     */
    void createNotification(Long userId, Integer type, String title, String content, Long relatedId);

    /**
     * 获取用户通知列表（分页）
     */
    Page<Notification> listUserNotifications(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户未读通知数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId);

    /**
     * 标记用户所有通知为已读
     */
    void markAllAsRead(Long userId);
}

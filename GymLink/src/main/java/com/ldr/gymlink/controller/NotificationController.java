package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.model.entity.Notification;
import com.ldr.gymlink.service.NotificationService;
import com.ldr.gymlink.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 通知管理
 */
@RestController
@RequestMapping("/notification")
@Tag(name = "通知管理")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @GetMapping("/list")
    @Operation(summary = "获取用户通知列表")
    public BaseResponse<Page<Notification>> listNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Notification> page = notificationService.listUserNotifications(userId, pageNum, pageSize);
        return ResultUtils.success(page);
    }

    @GetMapping("/unreadCount")
    @Operation(summary = "获取未读通知数量")
    public BaseResponse<Long> getUnreadCount(@RequestParam Long userId) {
        Long count = notificationService.getUnreadCount(userId);
        return ResultUtils.success(count);
    }

    @PostMapping("/markAsRead")
    @Operation(summary = "标记通知为已读")
    public BaseResponse<Boolean> markAsRead(@RequestParam Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResultUtils.success(true);
    }

    @PostMapping("/markAllAsRead")
    @Operation(summary = "标记所有通知为已读")
    public BaseResponse<Boolean> markAllAsRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return ResultUtils.success(true);
    }
}

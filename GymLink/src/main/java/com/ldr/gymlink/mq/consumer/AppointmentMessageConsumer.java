package com.ldr.gymlink.mq.consumer;

import com.ldr.gymlink.mq.config.RabbitMQConfig;
import com.ldr.gymlink.mq.message.AppointmentMessage;
import com.ldr.gymlink.service.NotificationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

/**
 * 预约消息消费者
 */
@Slf4j
@Component
public class AppointmentMessageConsumer {

    @Resource
    private NotificationService notificationService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @RabbitListener(queues = RabbitMQConfig.APPOINTMENT_NOTIFICATION_QUEUE)
    public void handleAppointmentNotification(AppointmentMessage message) {
        log.info("收到预约通知消息: type={}, appointmentId={}", message.getType(), message.getAppointmentId());

        try {
            String title;
            String content;
            String timeStr = DATE_FORMAT.format(message.getAppointTime()) + " - " + DATE_FORMAT.format(message.getEndTime());

            switch (message.getType()) {
                case AppointmentMessage.TYPE_NEW_APPOINTMENT:
                    // 通知教练有新预约
                    title = "新的预约申请";
                    content = String.format("学员 %s 向您发起了预约申请\n预约时间：%s\n留言：%s",
                            message.getSenderName(), timeStr, message.getMessage() != null ? message.getMessage() : "无");
                    break;

                case AppointmentMessage.TYPE_APPOINTMENT_CONFIRMED:
                    // 通知学员预约已确认
                    title = "预约已确认";
                    content = String.format("教练 %s 已确认您的预约\n预约时间：%s",
                            message.getSenderName(), timeStr);
                    break;

                case AppointmentMessage.TYPE_APPOINTMENT_REJECTED:
                    // 通知学员预约被拒绝
                    title = "预约被拒绝";
                    content = String.format("教练 %s 拒绝了您的预约申请\n预约时间：%s",
                            message.getSenderName(), timeStr);
                    break;

                case AppointmentMessage.TYPE_APPOINTMENT_CANCELLED:
                    // 通知对方预约已取消
                    title = "预约已取消";
                    content = String.format("%s 取消了预约\n预约时间：%s",
                            message.getSenderName(), timeStr);
                    break;

                default:
                    log.warn("未知的消息类型: {}", message.getType());
                    return;
            }

            // 创建通知记录
            notificationService.createNotification(
                    message.getReceiverUserId(),
                    1, // 预约通知类型
                    title,
                    content,
                    message.getAppointmentId()
            );

            log.info("预约通知创建成功: userId={}, title={}", message.getReceiverUserId(), title);

        } catch (Exception e) {
            log.error("处理预约通知消息失败: {}", e.getMessage(), e);
        }
    }
}

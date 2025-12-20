package com.ldr.gymlink.mq.consumer;

import com.ldr.gymlink.mq.config.RabbitMQConfig;
import com.ldr.gymlink.mq.message.CourseOrderMessage;
import com.ldr.gymlink.service.NotificationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 课程订单消息消费者
 */
@Slf4j
@Component
public class CourseOrderMessageConsumer {

    @Resource
    private NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.COURSE_ORDER_NOTIFICATION_QUEUE)
    public void handleCourseOrderNotification(CourseOrderMessage message) {
        log.info("收到课程订单通知消息: type={}, orderId={}", message.getType(), message.getOrderId());

        try {
            String title;
            String content;

            switch (message.getType()) {
                case CourseOrderMessage.TYPE_NEW_ORDER:
                    // 通知教练有新学员购买课程
                    title = "新的课程订单";
                    content = String.format("学员 %s 购买了您的课程\n课程：%s\n订单金额：¥%s",
                            message.getStudentName(),
                            message.getCourseName(),
                            message.getPrice());
                    break;

                default:
                    log.warn("未知的课程订单消息类型: {}", message.getType());
                    return;
            }

            // 创建通知记录
            notificationService.createNotification(
                    message.getCoachUserId(),
                    2, // 系统通知类型
                    title,
                    content,
                    message.getOrderId()
            );

            log.info("课程订单通知创建成功: coachUserId={}, title={}", message.getCoachUserId(), title);

        } catch (Exception e) {
            log.error("处理课程订单通知消息失败: {}", e.getMessage(), e);
        }
    }
}

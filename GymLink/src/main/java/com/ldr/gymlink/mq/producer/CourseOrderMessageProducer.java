package com.ldr.gymlink.mq.producer;

import com.ldr.gymlink.mq.config.RabbitMQConfig;
import com.ldr.gymlink.mq.message.CourseOrderMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 课程订单消息生产者
 */
@Slf4j
@Component
public class CourseOrderMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送课程订单通知消息
     */
    public void sendCourseOrderNotification(CourseOrderMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.COURSE_ORDER_EXCHANGE,
                    RabbitMQConfig.COURSE_ORDER_ROUTING_KEY,
                    message
            );
            log.info("发送课程订单通知消息成功: type={}, orderId={}, coachId={}",
                    message.getType(), message.getOrderId(), message.getCoachId());
        } catch (Exception e) {
            log.error("发送课程订单通知消息失败: {}", e.getMessage(), e);
        }
    }
}

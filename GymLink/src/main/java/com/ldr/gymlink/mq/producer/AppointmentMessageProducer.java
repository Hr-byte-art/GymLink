package com.ldr.gymlink.mq.producer;

import com.ldr.gymlink.mq.config.RabbitMQConfig;
import com.ldr.gymlink.mq.message.AppointmentMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 预约消息生产者
 */
@Slf4j
@Component
public class AppointmentMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送预约通知消息
     */
    public void sendAppointmentNotification(AppointmentMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.APPOINTMENT_EXCHANGE,
                    RabbitMQConfig.APPOINTMENT_ROUTING_KEY,
                    message
            );
            log.info("发送预约通知消息成功: type={}, appointmentId={}, receiverId={}",
                    message.getType(), message.getAppointmentId(), message.getReceiverUserId());
        } catch (Exception e) {
            log.error("发送预约通知消息失败: {}", e.getMessage(), e);
        }
    }
}

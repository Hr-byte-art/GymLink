package com.ldr.gymlink.mq.producer;

import com.ldr.gymlink.mq.config.RabbitMQConfig;
import com.ldr.gymlink.mq.message.RefundMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 退款消息生产者
 */
@Slf4j
@Component
public class RefundMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送退款通知消息
     */
    public void sendRefundNotification(RefundMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.REFUND_EXCHANGE,
                    RabbitMQConfig.REFUND_ROUTING_KEY,
                    message
            );
            log.info("发送退款通知消息成功: type={}, orderId={}, studentName={}",
                    message.getType(), message.getOrderId(), message.getStudentName());
        } catch (Exception e) {
            log.error("发送退款通知消息失败: {}", e.getMessage(), e);
        }
    }
}

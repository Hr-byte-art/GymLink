package com.ldr.gymlink.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 */
@Configuration
public class RabbitMQConfig {

    // 交换机名称
    public static final String APPOINTMENT_EXCHANGE = "gymlink.appointment.exchange";

    // 队列名称
    public static final String APPOINTMENT_NOTIFICATION_QUEUE = "gymlink.appointment.notification.queue";

    // 路由键
    public static final String APPOINTMENT_ROUTING_KEY = "appointment.notification";

    // 退款通知交换机
    public static final String REFUND_EXCHANGE = "gymlink.refund.exchange";

    // 退款通知队列
    public static final String REFUND_NOTIFICATION_QUEUE = "gymlink.refund.notification.queue";

    // 退款路由键
    public static final String REFUND_ROUTING_KEY = "refund.notification";

    // 课程订单交换机
    public static final String COURSE_ORDER_EXCHANGE = "gymlink.course.order.exchange";

    // 课程订单队列
    public static final String COURSE_ORDER_NOTIFICATION_QUEUE = "gymlink.course.order.notification.queue";

    // 课程订单路由键
    public static final String COURSE_ORDER_ROUTING_KEY = "course.order.notification";

    /**
     * JSON 消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 预约通知交换机
     */
    @Bean
    public DirectExchange appointmentExchange() {
        return new DirectExchange(APPOINTMENT_EXCHANGE, true, false);
    }

    /**
     * 预约通知队列
     */
    @Bean
    public Queue appointmentNotificationQueue() {
        return new Queue(APPOINTMENT_NOTIFICATION_QUEUE, true);
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding appointmentBinding(Queue appointmentNotificationQueue, DirectExchange appointmentExchange) {
        return BindingBuilder.bind(appointmentNotificationQueue)
                .to(appointmentExchange)
                .with(APPOINTMENT_ROUTING_KEY);
    }

    /**
     * 退款通知交换机
     */
    @Bean
    public DirectExchange refundExchange() {
        return new DirectExchange(REFUND_EXCHANGE, true, false);
    }

    /**
     * 退款通知队列
     */
    @Bean
    public Queue refundNotificationQueue() {
        return new Queue(REFUND_NOTIFICATION_QUEUE, true);
    }

    /**
     * 绑定退款队列到交换机
     */
    @Bean
    public Binding refundBinding(Queue refundNotificationQueue, DirectExchange refundExchange) {
        return BindingBuilder.bind(refundNotificationQueue)
                .to(refundExchange)
                .with(REFUND_ROUTING_KEY);
    }

    /**
     * 课程订单交换机
     */
    @Bean
    public DirectExchange courseOrderExchange() {
        return new DirectExchange(COURSE_ORDER_EXCHANGE, true, false);
    }

    /**
     * 课程订单队列
     */
    @Bean
    public Queue courseOrderNotificationQueue() {
        return new Queue(COURSE_ORDER_NOTIFICATION_QUEUE, true);
    }

    /**
     * 绑定课程订单队列到交换机
     */
    @Bean
    public Binding courseOrderBinding(Queue courseOrderNotificationQueue, DirectExchange courseOrderExchange) {
        return BindingBuilder.bind(courseOrderNotificationQueue)
                .to(courseOrderExchange)
                .with(COURSE_ORDER_ROUTING_KEY);
    }
}

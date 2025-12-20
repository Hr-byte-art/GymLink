package com.ldr.gymlink.config.rabbitmq;

/**
 * @Author 王哈哈
 * @Date 2025/11/8 00:45:00
 * @Description rabbitMq 路由键常量
 */
public class RabbitMqRoutingKeyConstant {
    
    // 场地预约 -- 管理员审批通过预约
    public static final String VENUE_BOOKING_APPROVE = "booking.approve";
    // 场地预约 -- 管理员审批拒绝预约
    public static final String VENUE_BOOKING_REJECT = "booking.reject";
    // 场地预约 -- 用户取消预约
    public static final String VENUE_BOOKING_CANCEL = "booking.cancel";
    // 场地预约 -- 用户创建预约后（延迟队列）
    public static final String VENUE_BOOKING_TIMEOUT = "booking.timeout";

    // 活动管理 -- 用户报名活动
    public static final String ACTIVITY_JOIN_NOTIFY = "activity.join";
    // 活动管理 -- 管理员审批通过
    public static final String ACTIVITY_APPROVE_NOTIFY = "activity.approve";
    // 活动管理 -- 管理员审批拒绝
    public static final String ACTIVITY_REJECT_NOTIFY = "activity.reject";
    
    // 跑腿订单 -- 创建订单
    public static final String ERRAND_ORDER_CREATED = "order.created";
    // 跑腿订单 -- 订单完成
    public static final String ERRAND_ORDER_COMPLETED = "order.completed";
    // 跑腿订单 -- 骑手接单
    public static final String ERRAND_ORDER_ACCEPTED = "order.accepted";
}


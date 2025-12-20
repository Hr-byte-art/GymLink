package com.ldr.gymlink.config.rabbitmq;

/**
 * @Author 王哈哈
 * @Date 2025/11/8 00:41:55
 * @Description rabbitMq队列常量
 */
public class RabbitMqQueueConstant {
    // 场地预约 -- 管理员审批通过预约
    public static final String VENUE_BOOKING_APPROVE =  "venue.booking.approve";
    // 场地预约 -- 管理员审批拒绝预约
    public static final String VENUE_BOOKING_REJECT =  "venue.booking.reject";
    // 场地预约 -- 用户取消预约
    public static final String VENUE_BOOKING_CANCEL =  "venue.booking.cancel";
    // 场地预约 -- 用户创建预约后 -- 延迟队列
    public static final String VENUE_BOOKING_TIMEOUT =  "venue.booking.timeout";

    // 活动管理 -- 用户报名活动
    public static final String ACTIVITY_JOIN_NOTIFY =  "activity.join.notify";
    // 活动管理 -- 管理员审批通过
    public static final String ACTIVITY_APPROVE_NOTIFY =  "activity.approve.notify";
    // 活动管理 -- 管理员审批拒绝
    public static final String ACTIVITY_REJECT_NOTIFY =  "activity.reject.notify";

    // 跑腿订单 -- 创建订单
    public static final String ERRAND_ORDER_CREATED =  "errand.order.created";
    // 跑腿订单 -- 订单完成
    public static final String ERRAND_ORDER_COMPLETED =  "errand.order.completed";
    // 跑腿订单 -- 骑手接单
    public static final String ERRAND_ORDER_ACCEPTED =  "errand.order.accepted";
}

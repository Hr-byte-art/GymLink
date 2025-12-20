package com.ldr.gymlink.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 预约消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息类型：1-新预约通知教练 2-预约确认通知学员 3-预约拒绝通知学员 4-预约取消通知
     */
    private Integer type;

    /**
     * 预约ID
     */
    private Long appointmentId;

    /**
     * 接收者用户ID
     */
    private Long receiverUserId;

    /**
     * 发送者用户ID
     */
    private Long senderUserId;

    /**
     * 发送者名称
     */
    private String senderName;

    /**
     * 接收者名称
     */
    private String receiverName;

    /**
     * 预约时间
     */
    private Date appointTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 附加消息
     */
    private String message;

    /**
     * 消息类型常量
     */
    public static final int TYPE_NEW_APPOINTMENT = 1;      // 新预约通知教练
    public static final int TYPE_APPOINTMENT_CONFIRMED = 2; // 预约确认通知学员
    public static final int TYPE_APPOINTMENT_REJECTED = 3;  // 预约拒绝通知学员
    public static final int TYPE_APPOINTMENT_CANCELLED = 4; // 预约取消通知
}

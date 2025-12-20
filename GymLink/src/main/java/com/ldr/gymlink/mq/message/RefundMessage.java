package com.ldr.gymlink.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退款消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息类型：1-退款申请通知管理员 2-退款通过通知学员 3-退款拒绝通知学员
     */
    private Integer type;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 学员用户ID（用于发送通知）
     */
    private Long studentUserId;

    /**
     * 学员姓名
     */
    private String studentName;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 消息类型常量
     */
    public static final int TYPE_REFUND_APPLY = 1;      // 退款申请通知管理员
    public static final int TYPE_REFUND_APPROVED = 2;   // 退款通过通知学员
    public static final int TYPE_REFUND_REJECTED = 3;   // 退款拒绝通知学员
}

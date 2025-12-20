package com.ldr.gymlink.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 课程订单消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseOrderMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息类型：1-新订单通知教练
     */
    private Integer type;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教练ID
     */
    private Long coachId;

    /**
     * 教练用户ID（用于发送通知）
     */
    private Long coachUserId;

    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 学员姓名
     */
    private String studentName;

    /**
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 消息类型常量
     */
    public static final int TYPE_NEW_ORDER = 1;  // 新订单通知教练
}

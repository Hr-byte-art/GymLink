package com.ldr.gymlink.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 已购课程视图
 */
@Data
public class PurchasedCourseVo implements Serializable {
    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程封面图
     */
    private String courseImage;

    /**
     * 课程类型
     */
    private String courseType;

    /**
     * 课程难度
     */
    private String difficulty;

    /**
     * 课程时长(分钟)
     */
    private Integer duration;

    /**
     * 教练ID
     */
    private Long coachId;

    /**
     * 教练姓名
     */
    private String coachName;

    /**
     * 购买价格
     */
    private BigDecimal price;

    /**
     * 购买时间
     */
    private Date purchaseTime;

    /**
     * 订单状态 1:已支付 2:已退款
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}

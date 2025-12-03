package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 课程购买订单表
 * @TableName course_order
 */
@TableName(value ="course_order")
@Data
public class CourseOrder implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    @TableField(value = "order_no")
    private String orderNo;

    /**
     * 学员ID
     */
    @TableField(value = "student_id")
    private Long studentId;

    /**
     * 课程ID
     */
    @TableField(value = "course_id")
    private Long courseId;

    /**
     * 冗余教练ID(方便查询)
     */
    @TableField(value = "coach_id")
    private Long coachId;

    /**
     * 成交价格
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 订单状态 1:已支付 2:已退款
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 购买时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
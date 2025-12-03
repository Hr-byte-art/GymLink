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
 * 充值记录表
 * @author 木子宸
 * @TableName recharge_record
 */
@TableName(value ="recharge_record")
@Data
public class RechargeRecord implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学员ID
     */
    @TableField(value = "student_id")
    private Long studentId;

    /**
     * 充值金额
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    /**
     * 充值时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
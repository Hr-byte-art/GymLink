package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 教练预约表
 * @TableName coach_appointment
 */
@TableName(value ="coach_appointment")
@Data
public class CoachAppointment implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学员ID
     */
    @TableField(value = "student_id")
    private Long studentId;

    /**
     * 预约的教练ID
     */
    @TableField(value = "coach_id")
    private Long coachId;

    /**
     * 预约的日期时间
     */
    @TableField(value = "appoint_time")
    private Date appointTime;

    /**
     * 备注信息
     */
    @TableField(value = "message")
    private String message;

    /**
     * 状态 0:待确认 1:已确认 2:已拒绝 3:已取消
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 提交时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
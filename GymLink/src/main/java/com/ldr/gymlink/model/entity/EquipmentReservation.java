package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 器材预约表
 * @TableName equipment_reservation
 */
@TableName(value ="equipment_reservation")
@Data
public class EquipmentReservation implements Serializable {
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
     * 器材ID
     */
    @TableField(value = "equipment_id")
    private Long equipmentId;

    /**
     * 开始使用时间
     */
    @TableField(value = "start_time")
    private Date startTime;

    /**
     * 结束使用时间
     */
    @TableField(value = "end_time")
    private Date endTime;

    /**
     * 状态 1:预约成功 2:已取消 3:已完成
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
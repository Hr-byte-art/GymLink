package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName(value = "course_booking")
public class CourseBooking implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "student_id")
    private Long studentId;

    @TableField(value = "course_id")
    private Long courseId;

    @TableField(value = "coach_id")
    private Long coachId;

    @TableField(value = "schedule_id")
    private Long scheduleId;

    @TableField(value = "order_id")
    private Long orderId;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

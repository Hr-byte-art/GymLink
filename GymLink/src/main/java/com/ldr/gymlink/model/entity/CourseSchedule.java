package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName(value = "course_schedule")
public class CourseSchedule implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "course_id")
    private Long courseId;

    @TableField(value = "coach_id")
    private Long coachId;

    @TableField(value = "start_time")
    private Date startTime;

    @TableField(value = "end_time")
    private Date endTime;

    @TableField(value = "capacity")
    private Integer capacity;

    @TableField(value = "booked_count")
    private Integer bookedCount;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

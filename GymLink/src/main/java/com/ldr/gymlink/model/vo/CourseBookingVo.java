package com.ldr.gymlink.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.model.entity.CourseBooking;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class CourseBookingVo extends CourseBooking {
    private String courseName;
    private String coachName;
    private Date scheduleStartTime;
    private Date scheduleEndTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

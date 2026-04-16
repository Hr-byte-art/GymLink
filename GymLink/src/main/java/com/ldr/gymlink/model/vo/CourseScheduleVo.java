package com.ldr.gymlink.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.model.entity.CourseSchedule;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CourseScheduleVo extends CourseSchedule {
    private String courseName;
    private String coachName;
    private Integer remainingCapacity;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

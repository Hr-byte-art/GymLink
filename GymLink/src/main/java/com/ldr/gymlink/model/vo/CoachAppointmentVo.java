package com.ldr.gymlink.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.model.entity.CoachAppointment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/12/1 22:13:00
 * @Description 教练预约VO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CoachAppointmentVo extends CoachAppointment {

    /**
     * 教练姓名
     */
    private String coachName;

    /**
     * 学员姓名
     */
    private String studentName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

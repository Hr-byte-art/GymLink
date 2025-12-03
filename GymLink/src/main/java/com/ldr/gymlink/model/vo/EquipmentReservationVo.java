package com.ldr.gymlink.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.model.entity.EquipmentReservation;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 22:15:00
 * @Description 器材预约VO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EquipmentReservationVo extends EquipmentReservation implements Serializable {

    /**
     * 器材名称
     */
    private String equipmentName;

    /**
     * 学员姓名
     */
    private String studentName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

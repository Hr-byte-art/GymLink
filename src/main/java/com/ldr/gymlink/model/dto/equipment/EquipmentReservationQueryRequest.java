package com.ldr.gymlink.model.dto.equipment;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 22:25:00
 * @Description 按器材查询预约记录请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EquipmentReservationQueryRequest extends PageRequest implements Serializable {

    /**
     * 器材ID
     */
    private Long equipmentId;

    /**
     * 状态 1:预约成功 2:已取消 3:已完成
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

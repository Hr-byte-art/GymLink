package com.ldr.gymlink.model.dto.equipment;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 22:25:00
 * @Description 按时间段查询预约记录请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EquipmentTimeRangeReservationQueryRequest extends PageRequest implements Serializable {

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 状态 1:预约成功 2:已取消 3:已完成
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

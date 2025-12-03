package com.ldr.gymlink.model.dto.equipment;

import lombok.Data;

import java.util.Date;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 21:50:33
 * @Description 预约健身器材请求
 */
@Data
public class BookingEquipmentRequest {
    /**
     * 器材id
     */
    private Long equipmentId;

    /**
     * 学员id
     */
    private Long studentId;

    /**
     * 预约开始时间
     */
    private Date startTime;

    /**
     * 预约结束时间
     */
    private Date endTime;
}

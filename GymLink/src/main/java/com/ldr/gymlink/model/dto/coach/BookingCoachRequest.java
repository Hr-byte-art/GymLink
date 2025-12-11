package com.ldr.gymlink.model.dto.coach;

import lombok.Data;

import java.util.Date;

/**
 * @Author 王哈哈
 * @Date 2025/12/1 22:13:00
 * @Description 预约教练请求
 */
@Data
public class BookingCoachRequest {
    /**
     * 教练id
     */
    private Long coachId;

    /**
     * 学员id
     */
    private Long studentId;

    /**
     * 预约开始时间
     */
    private Date appointTime;

    /**
     * 预约结束时间
     */
    private Date endTime;

    /**
     * 备注信息
     */
    private String message;
}

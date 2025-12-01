package com.ldr.gymlink.model.dto.coach;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author 王哈哈
 * @Date 2025/12/1 22:13:00
 * @Description 按学员和时间段查询教练预约记录请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StudentCoachTimeAppointmentSearchRequest extends PageRequest implements Serializable {

    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 状态 0:待确认 1:已确认 2:已拒绝 3:已取消
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

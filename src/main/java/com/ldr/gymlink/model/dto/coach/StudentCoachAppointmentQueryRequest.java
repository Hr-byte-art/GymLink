package com.ldr.gymlink.model.dto.coach;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/12/1 22:13:00
 * @Description 学员教练预约记录查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StudentCoachAppointmentQueryRequest extends PageRequest implements Serializable {

    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 状态 0:待确认 1:已确认 2:已拒绝 3:已取消
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

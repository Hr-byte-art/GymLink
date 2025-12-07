package com.ldr.gymlink.model.dto.student;

import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 已购课程查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PurchasedCourseQueryRequest extends PageRequest implements Serializable {
    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 课程名称（模糊搜索）
     */
    private String courseName;

    /**
     * 订单状态 1:已支付 2:已退款
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}

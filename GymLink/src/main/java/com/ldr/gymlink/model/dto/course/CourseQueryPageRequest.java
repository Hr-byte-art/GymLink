package com.ldr.gymlink.model.dto.course;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 21:18:00
 * @Description 课程分页查询请求
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class CourseQueryPageRequest extends PageRequest implements Serializable {

    /**
     * 课程名称
     */
    private String name;

    /**
     * 关联教练ID
     */
    private Integer coachId;

    /**
     * 难度等级
     */
    private String difficulty;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

    /**
     * 最短时长
     */
    private Integer minDuration;

    /**
     * 最长时长
     */
    private Integer maxDuration;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

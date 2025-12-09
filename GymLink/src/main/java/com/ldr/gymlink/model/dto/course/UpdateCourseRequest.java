package com.ldr.gymlink.model.dto.course;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 21:18:00
 * @Description 更新课程请求
 */
@Data
public class UpdateCourseRequest implements Serializable {

    /**
     * 课程名称
     */
    private String name;

    /**
     * 关联教练ID
     */
    private Long coachId;

    /**
     * 课程类型/分类
     */
    private String type;

    /**
     * 课程封面图
     */
    private String image;

    /**
     * 课程详情
     */
    private String description;

    /**
     * 课程价格
     */
    private BigDecimal price;

    /**
     * 课程时长(分钟)
     */
    private Integer duration;

    /**
     * 难度等级
     */
    private String difficulty;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

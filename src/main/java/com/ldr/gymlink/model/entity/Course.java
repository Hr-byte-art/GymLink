package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 健身课程表
 * @TableName course
 */
@TableName(value ="course")
@Data
public class Course implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 课程名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 关联教练ID
     */
    @TableField(value = "coach_id")
    private Integer coachId;

    /**
     * 课程封面图
     */
    @TableField(value = "image")
    private String image;

    /**
     * 课程详情
     */
    @TableField(value = "description")
    private String description;

    /**
     * 课程价格
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 课程时长(分钟)
     */
    @TableField(value = "duration")
    private Integer duration;

    /**
     * 难度等级
     */
    @TableField(value = "difficulty")
    private String difficulty;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
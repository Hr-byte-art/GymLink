package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 课程评价表
 * @TableName course_review
 */
@TableName(value ="course_review")
@Data
public class CourseReview implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学员ID
     */
    @TableField(value = "student_id")
    private Long studentId;

    /**
     * 课程ID（课程评价时使用）
     */
    @TableField(value = "course_id")
    private Long courseId;

    /**
     * 教练预约ID（预约评价时使用）
     */
    @TableField(value = "appointment_id")
    private Long appointmentId;

    /**
     * 教练ID
     */
    @TableField(value = "coach_id")
    private Long coachId;

    /**
     * 评价类型 1:课程评价 2:预约评价
     */
    @TableField(value = "review_type")
    private Integer reviewType;

    /**
     * 评分 1-5星
     */
    @TableField(value = "rating")
    private Integer rating;

    /**
     * 评价内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

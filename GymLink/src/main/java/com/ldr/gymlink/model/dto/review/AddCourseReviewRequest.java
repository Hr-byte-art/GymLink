package com.ldr.gymlink.model.dto.review;

import lombok.Data;
import java.io.Serializable;

/**
 * 添加评价请求（支持课程评价和预约评价）
 */
@Data
public class AddCourseReviewRequest implements Serializable {
    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 课程ID（课程评价时使用）
     */
    private Long courseId;

    /**
     * 教练预约ID（预约评价时使用）
     */
    private Long appointmentId;

    /**
     * 评价类型 1:课程评价 2:预约评价
     */
    private Integer reviewType;

    /**
     * 评分 1-5星
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}

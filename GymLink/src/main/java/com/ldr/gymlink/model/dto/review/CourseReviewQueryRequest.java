package com.ldr.gymlink.model.dto.review;

import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

/**
 * 课程评价查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CourseReviewQueryRequest extends PageRequest implements Serializable {
    /**
     * 教练ID
     */
    private Long coachId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 评分筛选
     */
    private Integer rating;

    private static final long serialVersionUID = 1L;
}

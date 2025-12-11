package com.ldr.gymlink.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 课程评价视图
 */
@Data
public class CourseReviewVo implements Serializable {
    /**
     * 评价ID
     */
    private Long id;

    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 学员姓名
     */
    private String studentName;

    /**
     * 学员头像
     */
    private String studentAvatar;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教练预约ID
     */
    private Long appointmentId;

    /**
     * 教练ID
     */
    private Long coachId;

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

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}

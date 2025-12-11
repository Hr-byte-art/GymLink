package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.review.AddCourseReviewRequest;
import com.ldr.gymlink.model.dto.review.CourseReviewQueryRequest;
import com.ldr.gymlink.model.entity.CourseReview;
import com.ldr.gymlink.model.vo.CourseReviewVo;

import java.util.Map;

/**
 * 课程评价服务
 */
public interface CourseReviewService extends IService<CourseReview> {

    /**
     * 添加课程评价
     *
     * @param request 添加评价请求
     * @return 评价信息
     */
    CourseReviewVo addReview(AddCourseReviewRequest request);

    /**
     * 分页查询评价列表
     *
     * @param request 查询请求
     * @return 评价列表
     */
    Page<CourseReviewVo> listReviewPage(CourseReviewQueryRequest request);

    /**
     * 检查学员是否可以评价某课程
     *
     * @param studentId 学员ID
     * @param courseId  课程ID
     * @return true-可以评价，false-不可以评价
     */
    boolean canReview(Long studentId, Long courseId);

    /**
     * 检查学员是否可以评价某预约
     *
     * @param studentId     学员ID
     * @param appointmentId 预约ID
     * @return true-可以评价，false-不可以评价
     */
    boolean canReviewAppointment(Long studentId, Long appointmentId);

    /**
     * 获取教练的评价统计
     *
     * @param coachId 教练ID
     * @return 统计数据（avgRating, reviewCount）
     */
    Map<String, Object> getCoachReviewStats(Long coachId);

    /**
     * 获取教练的学员数量
     *
     * @param coachId 教练ID
     * @return 学员数量
     */
    Integer getCoachStudentCount(Long coachId);

    /**
     * 获取教练的课程数量
     *
     * @param coachId 教练ID
     * @return 课程数量
     */
    Integer getCoachCourseCount(Long coachId);
}

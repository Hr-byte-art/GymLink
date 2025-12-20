package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.model.dto.review.AddCourseReviewRequest;
import com.ldr.gymlink.model.dto.review.CourseReviewQueryRequest;
import com.ldr.gymlink.model.vo.CourseReviewVo;
import com.ldr.gymlink.service.CourseReviewService;
import com.ldr.gymlink.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 课程评价控制器
 */
@RestController
@RequestMapping("/review")
@Tag(name = "课程评价管理")
public class CourseReviewController {

    @Resource
    private CourseReviewService courseReviewService;

    @PostMapping("/add")
    @Operation(summary = "添加课程评价")
    public BaseResponse<CourseReviewVo> addReview(@RequestBody AddCourseReviewRequest request) {
        CourseReviewVo reviewVo = courseReviewService.addReview(request);
        return ResultUtils.success(reviewVo);
    }

    @PostMapping("/list")
    @Operation(summary = "分页查询评价列表")
    public BaseResponse<Page<CourseReviewVo>> listReview(@RequestBody CourseReviewQueryRequest request) {
        Page<CourseReviewVo> page = courseReviewService.listReviewPage(request);
        return ResultUtils.success(page);
    }

    @GetMapping("/canReview")
    @Operation(summary = "检查是否可以评价课程")
    public BaseResponse<Boolean> canReview(@RequestParam Long studentId, @RequestParam Long courseId) {
        boolean canReview = courseReviewService.canReview(studentId, courseId);
        return ResultUtils.success(canReview);
    }

    @GetMapping("/canReviewAppointment")
    @Operation(summary = "检查是否可以评价预约")
    public BaseResponse<Boolean> canReviewAppointment(@RequestParam Long studentId, @RequestParam Long appointmentId) {
        boolean canReview = courseReviewService.canReviewAppointment(studentId, appointmentId);
        return ResultUtils.success(canReview);
    }

    @GetMapping("/coachStats")
    @Operation(summary = "获取教练评价统计")
    public BaseResponse<Map<String, Object>> getCoachReviewStats(@RequestParam Long coachId) {
        Map<String, Object> stats = courseReviewService.getCoachReviewStats(coachId);
        // 添加学员数和课程数
        stats.put("studentCount", courseReviewService.getCoachStudentCount(coachId));
        stats.put("courseCount", courseReviewService.getCoachCourseCount(coachId));
        return ResultUtils.success(stats);
    }

    @GetMapping("/courseStats")
    @Operation(summary = "获取课程评价统计")
    public BaseResponse<Map<String, Object>> getCourseReviewStats(@RequestParam Long courseId) {
        Map<String, Object> stats = courseReviewService.getCourseReviewStats(courseId);
        return ResultUtils.success(stats);
    }
}

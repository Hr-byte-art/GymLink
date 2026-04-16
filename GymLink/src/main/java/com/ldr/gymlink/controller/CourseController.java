package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.course.*;
import com.ldr.gymlink.model.entity.CourseSchedule;
import com.ldr.gymlink.model.vo.CourseScheduleVo;
import com.ldr.gymlink.model.vo.CourseStatisticsVo;
import com.ldr.gymlink.model.vo.CourseVo;
import com.ldr.gymlink.service.CourseScheduleService;
import com.ldr.gymlink.service.CourseService;
import com.ldr.gymlink.utils.ResultUtils;
import com.ldr.gymlink.utils.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/course")
@Tag(name = "课程管理")
public class CourseController {

    @Resource
    private CourseService courseService;

    @Resource
    private CourseScheduleService courseScheduleService;

    @PostMapping("/addCourse")
    @Operation(summary = "新增课程")
    public BaseResponse<CourseVo> addCourse(@RequestBody AddCourseRequest addCourseRequest) {
        return ResultUtils.success(courseService.addCourse(addCourseRequest));
    }

    @PostMapping("/listCourse")
    @Operation(summary = "分页查询课程")
    public BaseResponse<Page<CourseVo>> listCourse(@RequestBody CourseQueryPageRequest courseQueryPageRequest) {
        return ResultUtils.success(courseService.listCoursePage(courseQueryPageRequest));
    }

    @PostMapping("/deleteCourse")
    @Operation(summary = "删除课程")
    public BaseResponse<Boolean> deleteCourse(@RequestParam Long id) {
        return ResultUtils.success(courseService.deleteCourse(id));
    }

    @PostMapping("/updateCourse")
    @Operation(summary = "更新课程")
    public BaseResponse<Boolean> updateCourse(
            @RequestParam Long id,
            @RequestBody UpdateCourseRequest updateCourseRequest) {
        return ResultUtils.success(courseService.updateCourse(id, updateCourseRequest));
    }

    @GetMapping("/getCourseById")
    @Operation(summary = "获取课程详情")
    public BaseResponse<CourseVo> getCourseById(@RequestParam Long id) {
        return ResultUtils.success(courseService.getCourseById(id));
    }

    @GetMapping("/getStatistics")
    @Operation(summary = "获取课程统计数据")
    public BaseResponse<CourseStatisticsVo> getStatistics() {
        return ResultUtils.success(courseService.getCourseStatistics());
    }

    @PostMapping("/updateCourseImage")
    @Operation(summary = "更新课程封面")
    public BaseResponse<String> updateCourseImage(
            @RequestParam Long courseId,
            @RequestParam MultipartFile image) {
        ThrowUtils.throwIf(courseId == null, ErrorCode.PARAMS_ERROR, "课程ID不能为空");
        return ResultUtils.success(courseService.updateCourseImage(courseId, image));
    }

    @PostMapping("/addSchedule")
    @Operation(summary = "新增团课排期")
    public BaseResponse<CourseSchedule> addSchedule(@RequestBody AddCourseScheduleRequest request) {
        return ResultUtils.success(courseScheduleService.addCourseSchedule(request));
    }

    @PostMapping("/updateSchedule")
    @Operation(summary = "更新团课排期")
    public BaseResponse<Boolean> updateSchedule(
            @RequestParam Long id,
            @RequestBody UpdateCourseScheduleRequest request) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "排期ID不能为空");
        return ResultUtils.success(courseScheduleService.updateCourseSchedule(id, request));
    }

    @PostMapping("/deleteSchedule")
    @Operation(summary = "删除团课排期")
    public BaseResponse<Boolean> deleteSchedule(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "排期ID不能为空");
        return ResultUtils.success(courseScheduleService.deleteCourseSchedule(id));
    }

    @PostMapping("/listSchedule")
    @Operation(summary = "分页查询团课排期")
    public BaseResponse<Page<CourseScheduleVo>> listSchedule(@RequestBody CourseScheduleQueryRequest request) {
        return ResultUtils.success(courseScheduleService.listCourseSchedulePage(request));
    }

    @GetMapping("/listAvailableSchedules")
    @Operation(summary = "查询课程可预约排期")
    public BaseResponse<List<CourseScheduleVo>> listAvailableSchedules(@RequestParam Long courseId) {
        ThrowUtils.throwIf(courseId == null, ErrorCode.PARAMS_ERROR, "课程ID不能为空");
        return ResultUtils.success(courseScheduleService.listAvailableSchedulesByCourse(courseId));
    }
}

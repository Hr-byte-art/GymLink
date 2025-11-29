package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.model.dto.course.AddCourseRequest;
import com.ldr.gymlink.model.dto.course.CourseQueryPageRequest;
import com.ldr.gymlink.model.dto.course.UpdateCourseRequest;
import com.ldr.gymlink.model.vo.CourseVo;
import com.ldr.gymlink.service.CourseService;
import com.ldr.gymlink.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 21:02:33
 * @Description 课程管理
 */
@RestController
@RequestMapping("/course")
@Tag(name = "课程管理")
public class CourseController {

    @Resource
    private CourseService courseService;

    @PostMapping("/addCourse")
    @Operation(summary = "添加课程")
    public BaseResponse<CourseVo> addCourse(@RequestBody AddCourseRequest addCourseRequest) {
        CourseVo courseVo = courseService.addCourse(addCourseRequest);
        return ResultUtils.success(courseVo);
    }

    @PostMapping("/listCourse")
    @Operation(summary = "分页查询课程信息")
    public BaseResponse<Page<CourseVo>> listCourse(
            @RequestBody CourseQueryPageRequest courseQueryPageRequest) {
        Page<CourseVo> courseVos = courseService.listCoursePage(courseQueryPageRequest);
        return ResultUtils.success(courseVos);
    }

    @PostMapping("/deleteCourse")
    @Operation(summary = "删除课程信息")
    public BaseResponse<Boolean> deleteCourse(@RequestParam Integer id) {
        boolean delete = courseService.deleteCourse(id);
        return ResultUtils.success(delete);
    }

    @PostMapping("/updateCourse")
    @Operation(summary = "更新课程信息")
    public BaseResponse<Boolean> updateCourse(
            @RequestParam Integer id,
            @RequestBody UpdateCourseRequest updateCourseRequest) {
        boolean update = courseService.updateCourse(id, updateCourseRequest);
        return ResultUtils.success(update);
    }

    @GetMapping("/getCourseById")
    @Operation(summary = "获取课程信息")
    public BaseResponse<CourseVo> getCourseById(@RequestParam Integer id) {
        CourseVo courseVo = courseService.getCourseById(id);
        return ResultUtils.success(courseVo);
    }
}

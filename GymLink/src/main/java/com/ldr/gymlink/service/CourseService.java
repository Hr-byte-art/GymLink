package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.course.AddCourseRequest;
import com.ldr.gymlink.model.dto.course.CourseQueryPageRequest;
import com.ldr.gymlink.model.dto.course.UpdateCourseRequest;
import com.ldr.gymlink.model.entity.Course;
import com.ldr.gymlink.model.vo.CourseStatisticsVo;
import com.ldr.gymlink.model.vo.CourseVo;

/**
 * @author 木子宸
 * @description 针对表【course(健身课程表)】的数据库操作Service
 * @createDate 2025-11-30 20:57:06
 */
public interface CourseService extends IService<Course> {

    /**
     * 添加课程
     *
     * @param addCourseRequest 添加课程请求
     * @return 课程信息
     */
    CourseVo addCourse(AddCourseRequest addCourseRequest);

    /**
     * 获取所有课程信息(分页)
     *
     * @param courseQueryPageRequest 分页查询请求
     * @return 课程信息列表
     */
    Page<CourseVo> listCoursePage(CourseQueryPageRequest courseQueryPageRequest);

    /**
     * 删除课程
     *
     * @param id 课程id
     * @return 是否删除成功
     */
    boolean deleteCourse(Integer id);

    /**
     * 修改课程信息
     *
     * @param id                  课程id
     * @param updateCourseRequest 修改课程信息请求
     * @return 是否修改成功
     */
    boolean updateCourse(Integer id, UpdateCourseRequest updateCourseRequest);

    /**
     * 根据id获取课程信息
     *
     * @param id 课程id
     * @return 课程信息
     */
    CourseVo getCourseById(Integer id);

    /**
     * 获取课程统计数据
     *
     * @return 课程统计数据
     */
    CourseStatisticsVo getCourseStatistics();
}

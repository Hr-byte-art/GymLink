package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.course.AddCourseRequest;
import com.ldr.gymlink.model.dto.course.CourseQueryPageRequest;
import com.ldr.gymlink.model.dto.course.UpdateCourseRequest;
import com.ldr.gymlink.model.entity.Course;
import com.ldr.gymlink.model.vo.CourseVo;
import com.ldr.gymlink.service.CourseService;
import com.ldr.gymlink.mapper.CourseMapper;
import com.ldr.gymlink.utils.ThrowUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author 木子宸
 * @description 针对表【course(健身课程表)】的数据库操作Service实现
 * @createDate 2025-11-30 20:57:06
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course>
        implements CourseService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseVo addCourse(AddCourseRequest addCourseRequest) {
        ThrowUtils.throwIf(addCourseRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        Course course = new Course();
        BeanUtils.copyProperties(addCourseRequest, course);
        course.setCreateTime(new Date());
        boolean save = this.save(course);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加课程失败，请稍后重试");
        }

        CourseVo courseVo = new CourseVo();
        BeanUtils.copyProperties(course, courseVo);
        return courseVo;
    }

    @Override
    public Page<CourseVo> listCoursePage(CourseQueryPageRequest courseQueryPageRequest) {
        ThrowUtils.throwIf(courseQueryPageRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int pageSize = courseQueryPageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个课程");
        int pageNum = courseQueryPageRequest.getPageNum();

        LambdaQueryWrapper<Course> queryWrapper = getCourseQueryWrapper(courseQueryPageRequest);
        Page<Course> page = this.page(Page.of(pageNum, pageSize), queryWrapper);

        Page<CourseVo> courseVoPage = new Page<>(pageNum, pageSize);
        courseVoPage.setTotal(page.getTotal());
        courseVoPage.setRecords(page.getRecords().stream().map(course -> {
            CourseVo courseVo = new CourseVo();
            BeanUtils.copyProperties(course, courseVo);
            return courseVo;
        }).toList());
        return courseVoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourse(Integer id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "课程id不能为空");
        Course course = this.getById(id);
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        // 删除课程记录
        return this.removeById(id);
    }

    @Override
    public boolean updateCourse(Integer id, UpdateCourseRequest updateCourseRequest) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "课程id不能为空");
        Course course = this.getById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }
        BeanUtils.copyProperties(updateCourseRequest, course);
        return this.updateById(course);
    }

    @Override
    public CourseVo getCourseById(Integer id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "课程id不能为空");
        Course course = this.getById(id);
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        CourseVo courseVo = new CourseVo();
        BeanUtils.copyProperties(course, courseVo);
        return courseVo;
    }

    /**
     * 获取课程查询条件
     *
     * @param courseQueryPageRequest 查询条件
     * @return 查询条件
     */
    private LambdaQueryWrapper<Course> getCourseQueryWrapper(CourseQueryPageRequest courseQueryPageRequest) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        if (courseQueryPageRequest == null) {
            return queryWrapper;
        }

        String name = courseQueryPageRequest.getName();
        Integer coachId = courseQueryPageRequest.getCoachId();
        String difficulty = courseQueryPageRequest.getDifficulty();

        // 模糊查询课程名称
        queryWrapper.like(StringUtils.isNotBlank(name), Course::getName, name);
        // 精确查询教练ID
        queryWrapper.eq(coachId != null, Course::getCoachId, coachId);
        // 精确查询难度等级
        queryWrapper.eq(StringUtils.isNotBlank(difficulty), Course::getDifficulty, difficulty);
        // 价格范围查询
        queryWrapper.ge(courseQueryPageRequest.getMinPrice() != null,
                Course::getPrice, courseQueryPageRequest.getMinPrice());
        queryWrapper.le(courseQueryPageRequest.getMaxPrice() != null,
                Course::getPrice, courseQueryPageRequest.getMaxPrice());
        // 时长范围查询
        queryWrapper.ge(courseQueryPageRequest.getMinDuration() != null,
                Course::getDuration, courseQueryPageRequest.getMinDuration());
        queryWrapper.le(courseQueryPageRequest.getMaxDuration() != null,
                Course::getDuration, courseQueryPageRequest.getMaxDuration());

        return queryWrapper;
    }
}

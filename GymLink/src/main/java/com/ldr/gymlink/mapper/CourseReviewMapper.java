package com.ldr.gymlink.mapper;

import com.ldr.gymlink.model.entity.CourseReview;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【course_review(课程评价表)】的数据库操作Mapper
 */
@Mapper
public interface CourseReviewMapper extends BaseMapper<CourseReview> {

}

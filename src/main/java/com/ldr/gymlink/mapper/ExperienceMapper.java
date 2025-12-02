package com.ldr.gymlink.mapper;

import com.ldr.gymlink.model.entity.Experience;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 木子宸
 * @description 针对表【experience(健身经验/社区帖子表)】的数据库操作Mapper
 * @createDate 2025-12-02 20:39:04
 * @Entity com.ldr.gymlink.model.entity.Experience
 */
@Mapper
public interface ExperienceMapper extends BaseMapper<Experience> {

}

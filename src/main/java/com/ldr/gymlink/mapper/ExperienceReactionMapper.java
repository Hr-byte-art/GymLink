package com.ldr.gymlink.mapper;

import com.ldr.gymlink.model.entity.ExperienceReaction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 木子宸
* @description 针对表【experience_reaction(帖子点赞/点不喜欢记录表)】的数据库操作Mapper
* @createDate 2025-12-02 21:38:34
* @Entity com.ldr.gymlink.model.entity.ExperienceReaction
*/
@Mapper
public interface ExperienceReactionMapper extends BaseMapper<ExperienceReaction> {

}





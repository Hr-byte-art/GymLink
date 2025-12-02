package com.ldr.gymlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ldr.gymlink.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 木子宸
 * @description 针对表【comment(评论表)】的数据库操作Mapper
 * @createDate 2025-12-02 21:08:42
 * @Entity com.ldr.gymlink.model.entity.Comment
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

}

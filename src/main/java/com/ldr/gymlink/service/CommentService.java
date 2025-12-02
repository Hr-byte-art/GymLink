package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.comment.AddCommentRequest;
import com.ldr.gymlink.model.entity.Comment;
import com.ldr.gymlink.model.vo.CommentVo;

import java.util.List;

/**
 * @author 木子宸
 * @description 针对表【comment(评论表)】的数据库操作Service
 * @createDate 2025-12-02 21:08:42
 */
public interface CommentService extends IService<Comment> {

    /**
     * 添加评论（包括顶级评论和回复）
     *
     * @param addCommentRequest 添加评论请求
     * @return 评论信息
     */
    CommentVo addComment(AddCommentRequest addCommentRequest);

    /**
     * 删除评论（级联删除所有子评论）
     *
     * @param id 评论id
     * @return 是否删除成功
     */
    boolean deleteComment(Long id);

    /**
     * 获取某个经验的所有评论（树形结构）
     *
     * @param experienceId 经验id
     * @return 评论列表（树形）
     */
    List<CommentVo> getCommentTree(Long experienceId);

    /**
     * 获取某条评论的所有回复（平铺）
     *
     * @param parentId 父评论id
     * @return 回复列表
     */
    List<CommentVo> getReplies(Long parentId);

    /**
     * 点赞评论
     *
     * @param id 评论id
     * @return 是否成功
     */
    boolean likeComment(Long id);

    /**
     * 取消点赞
     *
     * @param id 评论id
     * @return 是否成功
     */
    boolean unlikeComment(Long id);
}

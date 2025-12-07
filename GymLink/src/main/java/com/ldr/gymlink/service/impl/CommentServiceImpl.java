package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.comment.AddCommentRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.Comment;
import com.ldr.gymlink.model.entity.Student;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.vo.CommentVo;
import com.ldr.gymlink.service.CoachService;
import com.ldr.gymlink.service.CommentService;
import com.ldr.gymlink.mapper.CommentMapper;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.service.UserService;
import com.ldr.gymlink.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 木子宸
 * @description 针对表【comment(评论表)】的数据库操作Service实现
 * @createDate 2025-12-02 21:08:42
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Resource
    private UserService userService;

    @Resource
    private StudentService studentService;

    @Resource
    private CoachService coachService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVo addComment(AddCommentRequest addCommentRequest) {
        ThrowUtils.throwIf(addCommentRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        ThrowUtils.throwIf(addCommentRequest.getContent() == null || addCommentRequest.getContent().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "评论内容不能为空");

        // 如果是回复评论，验证父评论是否存在
        if (addCommentRequest.getParentId() != null) {
            Comment parentComment = this.getById(addCommentRequest.getParentId());
            ThrowUtils.throwIf(parentComment == null, ErrorCode.NOT_FOUND_ERROR, "父评论不存在");
        }

        Comment comment = new Comment();
        BeanUtils.copyProperties(addCommentRequest, comment);
        comment.setCreateTime(new Date());
        comment.setLikeCount(0); // 初始化点赞数为0
        boolean save = this.save(comment);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加评论失败，请稍后重试");
        }

        CommentVo commentVo = new CommentVo();
        BeanUtils.copyProperties(comment, commentVo);
        commentVo.setReplies(new ArrayList<>());
        commentVo.setReplyCount(0);
        // 填充用户信息
        fillUserInfo(commentVo);
        return commentVo;
    }

    /**
     * 填充用户信息（名称和头像）
     */
    private void fillUserInfo(CommentVo commentVo) {
        if (commentVo == null || commentVo.getUserId() == null) {
            return;
        }
        Long userId = commentVo.getUserId();
        Integer userRole = commentVo.getUserRole();

        try {
            User user = userService.getById(userId);
            if (user != null && user.getAssociatedUserId() != null) {
                Long associatedUserId = user.getAssociatedUserId();
                if (userRole != null && userRole == 1) {
                    Coach coach = coachService.getById(associatedUserId);
                    if (coach != null) {
                        commentVo.setUserName(coach.getName());
                        commentVo.setUserAvatar(coach.getAvatar());
                    } else {
                        commentVo.setUserName(user.getUsername());
                    }
                } else {
                    Student student = studentService.getById(associatedUserId);
                    if (student != null) {
                        commentVo.setUserName(student.getName());
                        commentVo.setUserAvatar(student.getAvatar());
                    } else {
                        commentVo.setUserName(user.getUsername());
                    }
                }
            } else if (user != null) {
                commentVo.setUserName(user.getUsername());
            }
        } catch (Exception e) {
            commentVo.setUserName("用户" + userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "评论id不能为空");
        Comment comment = this.getById(id);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR, "评论不存在");

        // 级联删除所有子评论
        deleteCommentAndChildren(id);
        return true;
    }

    /**
     * 递归删除评论及其所有子评论
     */
    private void deleteCommentAndChildren(Long commentId) {
        // 查找所有子评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentId, commentId);
        List<Comment> children = this.list(queryWrapper);

        // 递归删除子评论
        for (Comment child : children) {
            deleteCommentAndChildren(child.getId());
        }

        // 删除当前评论
        this.removeById(commentId);
    }

    @Override
    public List<CommentVo> getCommentTree(Long experienceId) {
        ThrowUtils.throwIf(experienceId == null, ErrorCode.PARAMS_ERROR, "经验id不能为空");

        // 查询该经验的所有评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getExperienceId, experienceId);
        queryWrapper.orderByAsc(Comment::getCreateTime); // 按时间升序
        List<Comment> allComments = this.list(queryWrapper);

        // 转换为CommentVo
        Map<Long, CommentVo> commentMap = new HashMap<>();
        for (Comment comment : allComments) {
            CommentVo vo = new CommentVo();
            BeanUtils.copyProperties(comment, vo);
            vo.setReplies(new ArrayList<>());
            // 填充用户信息
            fillUserInfo(vo);
            commentMap.put(comment.getId(), vo);
        }

        // 构建树形结构
        List<CommentVo> rootComments = new ArrayList<>();
        for (Comment comment : allComments) {
            CommentVo vo = commentMap.get(comment.getId());
            if (comment.getParentId() == null) {
                // 顶级评论
                rootComments.add(vo);
            } else {
                // 子评论，添加到父评论的replies中
                CommentVo parent = commentMap.get(comment.getParentId());
                if (parent != null) {
                    parent.getReplies().add(vo);
                }
            }
        }

        // 计算每个评论的回复数量
        for (CommentVo vo : commentMap.values()) {
            vo.setReplyCount(vo.getReplies().size());
        }

        return rootComments;
    }

    @Override
    public List<CommentVo> getReplies(Long parentId) {
        ThrowUtils.throwIf(parentId == null, ErrorCode.PARAMS_ERROR, "父评论id不能为空");

        // 查询该评论的所有回复
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentId, parentId);
        queryWrapper.orderByAsc(Comment::getCreateTime); // 按时间升序
        List<Comment> replies = this.list(queryWrapper);

        // 转换为CommentVo
        return replies.stream().map(comment -> {
            CommentVo vo = new CommentVo();
            BeanUtils.copyProperties(comment, vo);
            vo.setReplies(new ArrayList<>());
            vo.setReplyCount(0);
            // 填充用户信息
            fillUserInfo(vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "评论id不能为空");
        Comment comment = this.getById(id);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR, "评论不存在");

        // 点赞数+1
        comment.setLikeCount(comment.getLikeCount() == null ? 1 : comment.getLikeCount() + 1);
        return this.updateById(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeComment(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "评论id不能为空");
        Comment comment = this.getById(id);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR, "评论不存在");

        // 点赞数-1，但不能小于0
        int currentLikeCount = comment.getLikeCount() == null ? 0 : comment.getLikeCount();
        comment.setLikeCount(Math.max(0, currentLikeCount - 1));
        return this.updateById(comment);
    }

    @Override
    public Long getCommentCount(Long experienceId) {
        if (experienceId == null) {
            return 0L;
        }
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getExperienceId, experienceId);
        return this.count(queryWrapper);
    }
}

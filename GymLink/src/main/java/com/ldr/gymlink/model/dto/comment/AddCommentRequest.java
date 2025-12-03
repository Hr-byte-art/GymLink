package com.ldr.gymlink.model.dto.comment;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 21:09:00
 * @Description 添加评论请求
 */
@Data
public class AddCommentRequest implements Serializable {

    /**
     * 评论内容
     */
    private String content;

    /**
     * 关联的健身经验ID
     */
    private Long experienceId;

    /**
     * 评论者ID
     */
    private Long userId;

    /**
     * 评论者角色 1:教练 2:学员
     */
    private Integer userRole;

    /**
     * 父评论ID（顶级评论时为null）
     */
    private Long parentId;

    /**
     * 被回复的用户ID（顶级评论时为null）
     */
    private Long replyToUserId;

    @Serial
    private static final long serialVersionUID = 1L;
}

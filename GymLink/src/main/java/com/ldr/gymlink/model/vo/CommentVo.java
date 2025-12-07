package com.ldr.gymlink.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 21:09:00
 * @Description 评论信息（支持树形结构）
 */
@Data
public class CommentVo implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

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
     * 评论者名称
     */
    private String userName;

    /**
     * 评论者头像
     */
    private String userAvatar;

    /**
     * 父评论ID（NULL表示顶级评论）
     */
    private Long parentId;

    /**
     * 被回复的用户ID
     */
    private Long replyToUserId;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 子评论列表（回复列表）
     */
    private List<CommentVo> replies;

    /**
     * 回复数量
     */
    private Integer replyCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

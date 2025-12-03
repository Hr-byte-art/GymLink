package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 评论表
 * @TableName comment
 */
@TableName(value ="comment")
@Data
public class Comment implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 关联的健身经验ID
     */
    @TableField(value = "experience_id")
    private Long experienceId;

    /**
     * 评论者ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 评论者角色 1:教练 2:学员
     */
    @TableField(value = "user_role")
    private Integer userRole;

    /**
     * 父评论ID（NULL表示顶级评论）
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 被回复的用户ID
     */
    @TableField(value = "reply_to_user_id")
    private Long replyToUserId;

    /**
     * 点赞数
     */
    @TableField(value = "like_count")
    private Integer likeCount;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
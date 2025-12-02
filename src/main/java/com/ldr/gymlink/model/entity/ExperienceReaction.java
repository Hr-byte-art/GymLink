package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 帖子点赞/点不喜欢记录表
 * @author 木子宸
 * @TableName experience_reaction
 */
@TableName(value ="experience_reaction")
@Data
public class ExperienceReaction implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的帖子ID（外键关联帖子表）
     */
    @TableField(value = "experience_id")
    private Long experienceId;

    /**
     * 操作用户ID（外键关联用户表）
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 反应类型：1=点赞，2=点不喜欢
     */
    @TableField(value = "reaction_type")
    private Integer reactionType;

    /**
     * 操作时间
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 更新时间（如取消后重新操作）
     */
    @TableField(value = "updated_at")
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
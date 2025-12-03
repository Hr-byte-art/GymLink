package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 健身经验/社区帖子表
 * 
 * @TableName experience
 */
@TableName(value = "experience")
@Data
public class Experience implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 经验内容(富文本)
     */
    @TableField(value = "content")
    private String content;

    /**
     * 发布者ID(教练或学员)
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 发布者角色 1:教练 2:学员
     */
    @TableField(value = "user_role")
    private Integer userRole;

    /**
     * 浏览量
     */
    @TableField(value = "view_count")
    private Long viewCount;

    /**
     * 发布时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
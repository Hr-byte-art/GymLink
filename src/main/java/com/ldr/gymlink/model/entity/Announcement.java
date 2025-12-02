package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 系统公告表
 * @TableName announcement
 */
@TableName(value ="announcement")
@Data
public class Announcement implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 公告内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 发布管理员ID
     */
    @TableField(value = "admin_id")
    private Long adminId;

    /**
     * 发布时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
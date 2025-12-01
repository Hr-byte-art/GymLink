package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 健身菜谱表
 * @TableName recipe
 */
@TableName(value ="recipe")
@Data
public class Recipe implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 菜谱标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 封面图片
     */
    @TableField(value = "cover_image")
    private String coverImage;

    /**
     * 菜谱内容(富文本)
     */
    @TableField(value = "content")
    private String content;

    /**
     * 标签(如:增肌,减脂)
     */
    @TableField(value = "tags")
    private String tags;

    /**
     * 发布人ID(管理员)
     */
    @TableField(value = "admin_id")
    private Long adminId;

    /**
     * 发布时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
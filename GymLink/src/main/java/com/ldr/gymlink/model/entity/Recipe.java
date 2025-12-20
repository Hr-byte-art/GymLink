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

    /**
     * 热量(千卡)
     */
    @TableField(value = "calories")
    private Integer calories;

    /**
     * 蛋白质(克)
     */
    @TableField(value = "protein")
    private Integer protein;

    /**
     * 碳水化合物(克)
     */
    @TableField(value = "carbs")
    private Integer carbs;

    /**
     * 脂肪(克)
     */
    @TableField(value = "fat")
    private Integer fat;

    /**
     * 准备时间(分钟)
     */
    @TableField(value = "prep_time")
    private Integer prepTime;

    /**
     * 烹饪时间(分钟)
     */
    @TableField(value = "cook_time")
    private Integer cookTime;

    /**
     * 份数
     */
    @TableField(value = "servings")
    private Integer servings;

    /**
     * 难度(easy/medium/hard)
     */
    @TableField(value = "difficulty")
    private String difficulty;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
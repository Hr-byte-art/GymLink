package com.ldr.gymlink.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 菜谱信息VO
 */
@Data
public class RecipeVo implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 菜谱标题
     */
    private String title;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 菜谱内容(富文本)
     */
    private String content;

    /**
     * 标签(如:增肌,减脂)
     */
    private String tags;

    /**
     * 发布人ID(管理员)
     */
    private Long adminId;

    /**
     * 发布时间
     */
    private Date createTime;

    /**
     * 热量(千卡)
     */
    private Integer calories;

    /**
     * 蛋白质(克)
     */
    private Integer protein;

    /**
     * 碳水化合物(克)
     */
    private Integer carbs;

    /**
     * 脂肪(克)
     */
    private Integer fat;

    /**
     * 准备时间(分钟)
     */
    private Integer prepTime;

    /**
     * 烹饪时间(分钟)
     */
    private Integer cookTime;

    /**
     * 份数
     */
    private Integer servings;

    /**
     * 难度(easy/medium/hard)
     */
    private String difficulty;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

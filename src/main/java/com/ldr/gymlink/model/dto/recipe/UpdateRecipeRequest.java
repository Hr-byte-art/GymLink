package com.ldr.gymlink.model.dto.recipe;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 更新菜谱请求
 */
@Data
public class UpdateRecipeRequest implements Serializable {

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

    private static final long serialVersionUID = 1L;
}

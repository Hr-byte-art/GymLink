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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

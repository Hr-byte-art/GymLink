package com.ldr.gymlink.model.dto.recipe;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author 木子宸
 * @Description 菜谱分页查询请求
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class RecipeQueryPageRequest extends PageRequest implements Serializable {

    /**
     * 菜谱标题
     */
    private String title;

    /**
     * 标签(如:增肌,减脂)
     */
    private String tags;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

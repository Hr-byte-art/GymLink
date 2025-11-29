package com.ldr.gymlink.model.dto.equipment;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 21:36:00
 * @Description 添加健身器材请求
 */
@Data
public class AddEquipmentRequest implements Serializable {

    /**
     * 器材名称
     */
    private String name;

    /**
     * 器材图片
     */
    private String image;

    /**
     * 放置位置
     */
    private String location;

    /**
     * 器材描述/使用说明
     */
    private String description;

    /**
     * 状态 1:正常 2:损坏/维护中
     */
    private Integer status;

    /**
     * 器材总数量
     */
    private Integer totalCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 健身器材表
 * @TableName equipment
 */
@TableName(value ="equipment")
@Data
public class Equipment implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 器材名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 器材图片
     */
    @TableField(value = "image")
    private String image;

    /**
     * 放置位置
     */
    @TableField(value = "location")
    private String location;

    /**
     * 器材描述/使用说明
     */
    @TableField(value = "description")
    private String description;

    /**
     * 状态 1:正常 2:损坏/维护中
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 器材总数量
     */
    @TableField(value = "total_count")
    private Integer totalCount;

    /**
     * 录入时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
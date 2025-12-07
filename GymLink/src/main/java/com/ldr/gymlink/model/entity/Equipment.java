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

    /**
     * 器材类型 1-有氧健身器材 2-力量训练器材 3-功能性训练器材 4-小型健身器械 5-康复与辅助器材 6-其他辅助设备 7-商用专用器材 8-家用专用器材
     * 1-1:跑步机
     * 1-2:椭圆机
     * 1-3:动感单车
     * 1-4:划船机
     * 1-5:健身车
     * 1-6:楼梯机
     * 1-7:体适能运动机
     * 2-1:固定器械
     * 2-2:自由重量器材
     * 2-3:综合训练器材
     */
    @TableField(value = "type")
    private String type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
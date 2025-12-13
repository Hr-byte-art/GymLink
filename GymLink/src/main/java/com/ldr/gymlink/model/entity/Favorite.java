package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 收藏表
 * @TableName favorite
 */
@TableName(value = "favorite")
@Data
public class Favorite implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学员ID
     */
    @TableField(value = "student_id")
    private Long studentId;

    /**
     * 收藏目标ID
     */
    @TableField(value = "target_id")
    private Long targetId;

    /**
     * 收藏类型 1:设施 2:教练 3:课程 4:食谱
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

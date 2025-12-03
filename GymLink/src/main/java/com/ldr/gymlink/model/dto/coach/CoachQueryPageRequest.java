package com.ldr.gymlink.model.dto.coach;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author 木子宸
 * @Date 2025/11/30 02:00:00
 * @Description 查询参数（带分页参数）
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class CoachQueryPageRequest extends PageRequest implements Serializable {

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别 1:男 2:女
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 最小年龄
     */
    private Integer minAge;

    /**
     * 最大年龄
     */
    private Integer maxAge;

    /**
     * 特长/专业方向
     */
    private String specialty;

    /**
     * 入驻时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}

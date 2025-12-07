package com.ldr.gymlink.model.dto.coach;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 木子宸
 * @Date 2025/11/30 02:00:00
 * @Description 修改教练信息
 */
@Data
public class UpdateCoachRequest implements Serializable {

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
     * 年龄
     */
    private Integer age;

    /**
     * 特长/专业方向
     */
    private String specialty;

    /**
     * 个人简介
     */
    private String intro;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

package com.ldr.gymlink.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 00:06:12
 * @Description 教练视图
 */
@Data
public class CoachVo implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 账号
     */
    private String username;


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
     * 头像
     */
    private String avatar;

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

    /**
     * 入驻时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

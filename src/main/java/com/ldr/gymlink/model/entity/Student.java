package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 学员/用户信息表
 * @TableName student
 */
@TableName(value ="student")
@Data
public class Student implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 账号
     */
    @TableField(value = "username")
    private String username;

    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 昵称/姓名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 性别 1:男 2:女 3:未知
     */
    @TableField(value = "gender")
    private Integer gender;

    /**
     * 电话
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 头像
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 身高(cm)
     */
    @TableField(value = "height")
    private BigDecimal height;

    /**
     * 体重(kg)
     */
    @TableField(value = "weight")
    private BigDecimal weight;

    /**
     * 账户余额(用于购买课程)
     */
    @TableField(value = "balance")
    private BigDecimal balance;

    /**
     * 注册时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
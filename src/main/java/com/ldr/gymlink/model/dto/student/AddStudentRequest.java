package com.ldr.gymlink.model.dto.student;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author 木子宸
 * @Date 2025/11/30 01:01:00
 * @Description 添加学员请求
 */
@Data
public class AddStudentRequest implements Serializable {

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称/姓名
     */
    private String name;

    /**
     * 性别 1:男 2:女 3:未知
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
     * 身高(cm)
     */
    private BigDecimal height;

    /**
     * 体重(kg)
     */
    private BigDecimal weight;

    /**
     * 账户余额(用于购买课程)
     */
    private BigDecimal balance;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

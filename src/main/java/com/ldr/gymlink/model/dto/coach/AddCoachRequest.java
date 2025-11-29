package com.ldr.gymlink.model.dto.coach;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 00:34:06
 * @Description 添加教练请求
 */
@Data
public class AddCoachRequest implements Serializable {

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

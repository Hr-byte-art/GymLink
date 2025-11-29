package com.ldr.gymlink.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author 王哈哈
 * @Date 2025/11/29 23:43:18
 * @Description 学员视图
 */
@Data
public class StudentVo {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 账号
     */
    private String username;

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

    /**
     * 注册时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

package com.ldr.gymlink.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 00:07:10
 * @Description 管理员视图
 */
@Data
public class AdminVo {
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
     * 联系电话
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

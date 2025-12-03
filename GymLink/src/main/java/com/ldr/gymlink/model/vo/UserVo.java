package com.ldr.gymlink.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @Author 王哈哈
 * @Date 2025/11/29 16:19:39
 * @Description 用户视图
 */
@Data
public class UserVo {
    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 状态 1正常 0禁用
     */
    private Integer status;

    /**
     * 用户角色 0-管理员 1-教练 2-普通用户
     */
    private String role;


    /**
     * 创建时间
     */
    private Date createTime;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

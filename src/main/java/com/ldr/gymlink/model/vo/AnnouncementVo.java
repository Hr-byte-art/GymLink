package com.ldr.gymlink.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 20:49:00
 * @Description 公告信息
 */
@Data
public class AnnouncementVo implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 发布管理员ID
     */
    private Long adminId;

    /**
     * 发布时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

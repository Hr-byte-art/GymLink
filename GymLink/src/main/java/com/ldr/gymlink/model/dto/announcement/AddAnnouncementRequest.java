package com.ldr.gymlink.model.dto.announcement;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 20:49:00
 * @Description 添加公告请求
 */
@Data
public class AddAnnouncementRequest implements Serializable {

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

    @Serial
    private static final long serialVersionUID = 1L;
}

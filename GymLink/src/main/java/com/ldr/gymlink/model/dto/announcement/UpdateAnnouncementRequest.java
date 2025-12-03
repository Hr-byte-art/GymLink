package com.ldr.gymlink.model.dto.announcement;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 20:49:00
 * @Description 更新公告请求
 */
@Data
public class UpdateAnnouncementRequest implements Serializable {

    /**
     * 公告id
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

    @Serial
    private static final long serialVersionUID = 1L;
}

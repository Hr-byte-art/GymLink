package com.ldr.gymlink.model.dto.experience;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 21:00:00
 * @Description 更新健身经验请求
 */
@Data
public class UpdateExperienceRequest implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 经验内容(富文本)
     */
    private String content;

    @Serial
    private static final long serialVersionUID = 1L;
}

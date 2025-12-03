package com.ldr.gymlink.model.dto.comment;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 21:09:00
 * @Description 评论查询请求
 */
@Data
public class CommentQueryRequest implements Serializable {

    /**
     * 健身经验ID
     */
    private Long experienceId;

    /**
     * 父评论ID（查询某条评论的回复）
     */
    private Long parentId;

    @Serial
    private static final long serialVersionUID = 1L;
}

package com.ldr.gymlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * AI助手对话记忆表
 * @author 木子宸
 * @TableName chat_memory
 */
@TableName(value ="chat_memory")
@Data
public class ChatMemory implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联的用户/会员ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 存储完整的 List<ChatMessage> 序列化数据
     */
    @TableField(value = "messages")
    private String messages;

    /**
     * 当前存储的消息条数 (方便做统计)
     */
    @TableField(value = "message_count")
    private Integer messageCount;

    /**
     * 估算的 token 消耗 (可选，用于成本分析)
     */
    @TableField(value = "total_tokens")
    private Integer totalTokens;

    /**
     * 首次对话时间
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 最后活跃时间
     */
    @TableField(value = "updated_at")
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
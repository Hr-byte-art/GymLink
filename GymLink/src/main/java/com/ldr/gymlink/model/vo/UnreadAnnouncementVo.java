package com.ldr.gymlink.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 未读公告响应
 */
@Data
public class UnreadAnnouncementVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 最新的未读公告
     */
    private AnnouncementVo announcement;

    /**
     * 7天内未读公告总数（包含当前这条）
     */
    private Integer unreadCount;

    /**
     * 错过的公告数量（不包含当前这条）
     */
    private Integer missedCount;
}

package com.ldr.gymlink.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 21:00:00
 * @Description 健身经验信息
 */
@Data
public class ExperienceVo implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 经验内容(富文本)
     */
    private String content;

    /**
     * 发布者ID(教练或学员)
     */
    private Long userId;

    /**
     * 发布者角色 1:教练 2:学员
     */
    private Integer userRole;

    /**
     * 发布者名称
     */
    private String userName;

    /**
     * 发布者头像
     */
    private String userAvatar;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 当前用户的反应类型 (0=不喜欢, 1=点赞, null=无反应)
     */
    private Integer userReactionType;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 发布时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

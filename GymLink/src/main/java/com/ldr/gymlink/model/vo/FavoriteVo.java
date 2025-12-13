package com.ldr.gymlink.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 收藏信息VO
 */
@Data
public class FavoriteVo {
    /**
     * 收藏ID
     */
    private Long id;

    /**
     * 收藏目标ID
     */
    private Long targetId;

    /**
     * 收藏类型 1:设施 2:教练 3:课程 4:食谱
     */
    private Integer type;

    /**
     * 收藏类型名称
     */
    private String typeName;

    /**
     * 目标名称
     */
    private String targetName;

    /**
     * 目标图片
     */
    private String targetImage;

    /**
     * 目标描述
     */
    private String targetDescription;

    /**
     * 创建时间
     */
    private Date createTime;
}

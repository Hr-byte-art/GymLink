package com.ldr.gymlink.model.dto.favorite;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 收藏/取消收藏请求
 */
@Data
public class FavoriteRequest {
    /**
     * 收藏目标ID（使用字符串避免大数精度丢失）
     */
    @NotBlank(message = "目标ID不能为空")
    private String targetId;

    /**
     * 收藏类型 1:设施 2:教练 3:课程 4:食谱
     */
    @NotNull(message = "收藏类型不能为空")
    private Integer type;

    /**
     * 获取目标ID的Long值
     */
    public Long getTargetIdAsLong() {
        return Long.parseLong(targetId);
    }
}

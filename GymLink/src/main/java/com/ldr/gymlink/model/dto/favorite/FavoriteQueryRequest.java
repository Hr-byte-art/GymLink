package com.ldr.gymlink.model.dto.favorite;

import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏列表查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FavoriteQueryRequest extends PageRequest {
    /**
     * 收藏类型 1:设施 2:教练 3:课程 4:食谱
     */
    private Integer type;
}

package com.ldr.gymlink.model.dto.announcement;

import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 20:49:00
 * @Description 公告分页查询请求
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class AnnouncementQueryPageRequest extends PageRequest implements Serializable {

    /**
     * 公告标题
     */
    private String title;

    /**
     * 发布管理员ID
     */
    private Long adminId;

    /**
     * 时间范围（天数）：7=7天内, 15=半个月内, 30=一个月内, 90=一季度内, 365=一年内
     */
    private Integer timeRange;

    private static final long serialVersionUID = 1L;
}

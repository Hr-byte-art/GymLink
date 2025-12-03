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

    private static final long serialVersionUID = 1L;
}

package com.ldr.gymlink.model.dto.experience;

import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 21:00:00
 * @Description 健身经验分页查询请求
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ExperienceQueryPageRequest extends PageRequest implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 发布者ID(教练或学员)
     */
    private Long userId;

    /**
     * 发布者角色 1:教练 2:学员
     */
    private Integer userRole;

    private static final long serialVersionUID = 1L;
}

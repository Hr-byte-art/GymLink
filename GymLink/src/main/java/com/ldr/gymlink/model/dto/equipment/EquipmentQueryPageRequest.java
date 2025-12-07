package com.ldr.gymlink.model.dto.equipment;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 21:36:00
 * @Description 健身器材分页查询请求
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class EquipmentQueryPageRequest extends PageRequest implements Serializable {

    /**
     * 器材名称
     */
    private String name;

    /**
     * 放置位置
     */
    private String location;

    /**
     * 设备类型
     */
    private String type;

    /**
     * 状态 1:正常 2:损坏/维护中
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

package com.ldr.gymlink.model.dto.equipment;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 22:15:00
 * @Description 所有预约记录查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AllReservationQueryRequest extends PageRequest implements Serializable {

    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 器材ID
     */
    private Long equipmentId;

    /**
     * 状态 1:预约成功 2:已取消 3:已完成
     */
    private Integer status;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

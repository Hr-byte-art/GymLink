package com.ldr.gymlink.model.dto.student;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ldr.gymlink.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 01:21:02
 * @Description 查询参数（带分页参数）
 */
@Data
public class StudentQueryPageRequest extends PageRequest implements Serializable {

    /**
     * 昵称/姓名
     */
    private String name;

    /**
     * 性别 1:男 2:女 3:未知
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 最高身高(cm)
     */
    private BigDecimal maxHeight;

    /**
     * 最低体重(kg)
     */
    private BigDecimal minHeight;

    /**
     * 最大体重(kg)
     */
    private BigDecimal maxWeight;

    /**
     * 最小体重(kg)
     */
    private BigDecimal minWeight;

    /**
     * 注册时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}

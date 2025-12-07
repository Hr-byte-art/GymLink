package com.ldr.gymlink.model.dto.student;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 01:53:11
 * @Description 修改学员信息
 */
@Data
public class UpdateStudentRequest implements Serializable {

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
     * 身高(cm)
     */
    private BigDecimal height;

    /**
     * 体重(kg)
     */
    private BigDecimal weight;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

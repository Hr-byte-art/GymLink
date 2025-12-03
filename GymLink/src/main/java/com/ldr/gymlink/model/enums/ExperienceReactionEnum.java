package com.ldr.gymlink.model.enums;

import lombok.Getter;

/**
 * 帖子反应枚举（点赞/不喜欢）
 * 编码规则：0=不喜欢，1=喜欢
 * @author 木子宸
 */
@Getter
public enum ExperienceReactionEnum {
    // 枚举项：中文描述 + 英文标识 + int编码
    DISLIKE("不喜欢", "dislike", 0),
    LIKE("点赞", "like", 1);

    // 中文描述（用于前端展示、日志打印）
    private final String desc;
    // 英文标识（可选，用于接口交互、存储冗余）
    private final String code;
    // 核心int编码（用于数据库存储、高效查询）
    private final int value;

    // 构造方法：绑定三个维度的信息
    ExperienceReactionEnum(String desc, String code, int value) {
        this.desc = desc;
        this.code = code;
        this.value = value;
    }

    /**
     * 根据 int 编码获取枚举（最常用：数据库查询后转枚举）
     */
    public static ExperienceReactionEnum getByValue(int value) {
        for (ExperienceReactionEnum enumItem : values()) {
            if (enumItem.getValue() == value) {
                return enumItem;
            }
        }
        // 找不到返回null，或抛出IllegalArgumentException（根据业务选择）
        throw new IllegalArgumentException("无效的帖子反应编码：" + value);
    }

    /**
     * 根据英文标识获取枚举（可选：接口交互时用）
     */
    public static ExperienceReactionEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ExperienceReactionEnum enumItem : values()) {
            if (enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        return null;
    }

    /**
     * 根据中文描述获取枚举（可选：前端筛选时用）
     */
    public static ExperienceReactionEnum getByDesc(String desc) {
        if (desc == null) {
            return null;
        }
        for (ExperienceReactionEnum enumItem : values()) {
            if (enumItem.getDesc().equals(desc)) {
                return enumItem;
            }
        }
        return null;
    }
}
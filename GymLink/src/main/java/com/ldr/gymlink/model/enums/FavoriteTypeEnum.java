package com.ldr.gymlink.model.enums;

import lombok.Getter;

/**
 * 收藏类型枚举
 */
@Getter
public enum FavoriteTypeEnum {
    EQUIPMENT(1, "设施"),
    COACH(2, "教练"),
    COURSE(3, "课程"),
    RECIPE(4, "食谱");

    private final Integer value;
    private final String desc;

    FavoriteTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static FavoriteTypeEnum getByValue(Integer value) {
        for (FavoriteTypeEnum type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}

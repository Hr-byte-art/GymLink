package com.ldr.gymlink.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Author 王哈哈
 * @Date 2025/12/07
 * @Description 器材使用统计VO
 */
@Data
public class EquipmentStatisticsVo implements Serializable {

    /**
     * 器材总数
     */
    private Long totalEquipment;

    /**
     * 正常状态器材数
     */
    private Long normalCount;

    /**
     * 维护中器材数
     */
    private Long maintenanceCount;

    /**
     * 今日预约数
     */
    private Long todayReservationCount;

    /**
     * 本周预约数
     */
    private Long weekReservationCount;

    /**
     * 本月预约数
     */
    private Long monthReservationCount;

    /**
     * 各类型器材数量统计
     */
    private List<TypeCountVo> typeStatistics;

    /**
     * 最近7天每日预约趋势
     */
    private List<DailyCountVo> dailyReservationTrend;

    /**
     * 热门器材TOP10（按预约次数）
     */
    private List<EquipmentRankVo> hotEquipmentRank;

    @Serial
    private static final long serialVersionUID = 1L;

    @Data
    public static class TypeCountVo implements Serializable {
        private String type;
        private String typeName;
        private Long count;
        @Serial
        private static final long serialVersionUID = 1L;
    }

    @Data
    public static class DailyCountVo implements Serializable {
        private String date;
        private Long count;
        @Serial
        private static final long serialVersionUID = 1L;
    }

    @Data
    public static class EquipmentRankVo implements Serializable {
        private Long equipmentId;
        private String equipmentName;
        private Long reservationCount;
        @Serial
        private static final long serialVersionUID = 1L;
    }
}

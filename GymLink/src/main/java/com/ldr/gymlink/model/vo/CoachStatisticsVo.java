package com.ldr.gymlink.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author 王哈哈
 * @Date 2025/12/07
 * @Description 教练统计VO
 */
@Data
public class CoachStatisticsVo implements Serializable {

    /**
     * 教练总数
     */
    private Long totalCoach;

    /**
     * 男教练数
     */
    private Long maleCount;

    /**
     * 女教练数
     */
    private Long femaleCount;

    /**
     * 今日预约数
     */
    private Long todayAppointmentCount;

    /**
     * 本周预约数
     */
    private Long weekAppointmentCount;

    /**
     * 本月预约数
     */
    private Long monthAppointmentCount;

    /**
     * 待确认预约数
     */
    private Long pendingAppointmentCount;

    /**
     * 已确认预约数
     */
    private Long confirmedAppointmentCount;

    /**
     * 各专长教练数量统计
     */
    private List<SpecialtyCountVo> specialtyStatistics;

    /**
     * 最近7天每日预约趋势
     */
    private List<DailyCountVo> dailyAppointmentTrend;

    /**
     * 热门教练TOP10（按预约次数）
     */
    private List<CoachRankVo> hotCoachRank;

    /**
     * 教练年龄分布
     */
    private List<AgeDistributionVo> ageDistribution;

    @Serial
    private static final long serialVersionUID = 1L;

    @Data
    public static class SpecialtyCountVo implements Serializable {
        private String specialty;
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
    public static class CoachRankVo implements Serializable {
        private Long coachId;
        private String coachName;
        private String specialty;
        private Long appointmentCount;
        @Serial
        private static final long serialVersionUID = 1L;
    }

    @Data
    public static class AgeDistributionVo implements Serializable {
        private String ageRange;
        private Long count;
        @Serial
        private static final long serialVersionUID = 1L;
    }
}

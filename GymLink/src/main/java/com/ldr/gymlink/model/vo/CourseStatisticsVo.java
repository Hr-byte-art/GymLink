package com.ldr.gymlink.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author 王哈哈
 * @Date 2025/12/07
 * @Description 课程统计VO
 */
@Data
public class CourseStatisticsVo implements Serializable {

    /**
     * 课程总数
     */
    private Long totalCourse;

    /**
     * 今日购买数
     */
    private Long todayOrderCount;

    /**
     * 本周购买数
     */
    private Long weekOrderCount;

    /**
     * 本月购买数
     */
    private Long monthOrderCount;

    /**
     * 总销售额
     */
    private BigDecimal totalRevenue;

    /**
     * 本月销售额
     */
    private BigDecimal monthRevenue;

    /**
     * 各难度课程数量统计
     */
    private List<DifficultyCountVo> difficultyStatistics;

    /**
     * 各类型课程数量统计
     */
    private List<TypeCountVo> typeStatistics;

    /**
     * 最近7天每日购买趋势
     */
    private List<DailyCountVo> dailyOrderTrend;

    /**
     * 热门课程TOP10（按购买次数）
     */
    private List<CourseRankVo> hotCourseRank;

    /**
     * 热门教练TOP10（按课程销量）
     */
    private List<CoachRankVo> hotCoachRank;

    @Serial
    private static final long serialVersionUID = 1L;

    @Data
    public static class DifficultyCountVo implements Serializable {
        private String difficulty;
        private String difficultyName;
        private Long count;
        @Serial
        private static final long serialVersionUID = 1L;
    }

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
        private BigDecimal revenue;
        @Serial
        private static final long serialVersionUID = 1L;
    }

    @Data
    public static class CourseRankVo implements Serializable {
        private Long courseId;
        private String courseName;
        private Long orderCount;
        private BigDecimal totalRevenue;
        @Serial
        private static final long serialVersionUID = 1L;
    }

    @Data
    public static class CoachRankVo implements Serializable {
        private Long coachId;
        private String coachName;
        private Long orderCount;
        private BigDecimal totalRevenue;
        @Serial
        private static final long serialVersionUID = 1L;
    }
}

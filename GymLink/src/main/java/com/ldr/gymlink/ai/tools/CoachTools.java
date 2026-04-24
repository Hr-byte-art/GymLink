package com.ldr.gymlink.ai.tools;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.model.dto.coach.CoachQueryPageRequest;
import com.ldr.gymlink.model.vo.CoachVo;
import com.ldr.gymlink.service.CoachService;
import com.ldr.gymlink.service.CourseReviewService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 教练相关 AI 工具
 */
@Component
@Slf4j
public class CoachTools {

    @Resource
    private CoachService coachService;

    @Resource
    private CourseReviewService courseReviewService;

    @Tool("根据教练专业方向搜索教练。专业方向编码：1=私人健身教练，2=团体课教练，3=力量训练，4=瑜伽，5=有氧运动，6=康复/矫正训练教练，7=营养与生活方式教练，8=专项运动教练，9=线上健身教练。")
    public String searchCoaches(
            @P("教练专业方向编码，可为空") String specialty) {
        log.info("AI工具调用: searchCoaches, specialty={}", specialty);

        CoachQueryPageRequest request = new CoachQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);
        request.setSpecialty(specialty);

        List<CoachVo> coaches = coachService.listCoachPage(request).getRecords();
        if (coaches.isEmpty()) {
            return "没有找到符合条件的教练。";
        }

        StringBuilder sb = new StringBuilder("找到以下教练：\n");
        for (CoachVo coach : coaches) {
            sb.append(String.format("- [%s] %s，预约价格：%s / 小时，简介：%s%n",
                    getSpecialtyName(coach.getSpecialty()),
                    defaultText(coach.getName(), "未命名教练"),
                    formatPrice(coach.getPrice()),
                    summarizeIntro(coach.getIntro())));
        }
        return sb.toString();
    }

    @Tool("根据教练姓名关键词搜索教练")
    public String searchCoachesByName(@P("教练姓名关键词") String name) {
        log.info("AI工具调用: searchCoachesByName, name={}", name);

        CoachQueryPageRequest request = new CoachQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);
        request.setName(name);

        List<CoachVo> coaches = coachService.listCoachPage(request).getRecords();
        if (coaches.isEmpty()) {
            return "没有找到名称包含“" + name + "”的教练。";
        }

        StringBuilder sb = new StringBuilder("找到以下教练：\n");
        for (CoachVo coach : coaches) {
            sb.append(String.format("- [%s] %s，预约价格：%s / 小时%n",
                    getSpecialtyName(coach.getSpecialty()),
                    defaultText(coach.getName(), "未命名教练"),
                    formatPrice(coach.getPrice())));
        }
        return sb.toString();
    }

    @Tool("获取教练详情，包括专业方向、年龄、简介和评分等信息")
    public String getCoachDetail(@P("教练姓名") String coachName) {
        log.info("AI工具调用: getCoachDetail, coachName={}", coachName);

        CoachQueryPageRequest request = new CoachQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setName(coachName);

        List<CoachVo> coaches = coachService.listCoachPage(request).getRecords();
        if (coaches.isEmpty()) {
            return "没有找到名为“" + coachName + "”的教练。";
        }

        CoachVo coach = coaches.get(0);
        String ratingInfo = "暂无评价";
        try {
            Map<String, Object> stats = courseReviewService.getCoachReviewStats(coach.getId());
            Double avgRating = stats == null ? null : (Double) stats.get("avgRating");
            Integer reviewCount = stats == null ? null : (Integer) stats.get("reviewCount");
            if (avgRating != null && reviewCount != null && reviewCount > 0) {
                ratingInfo = String.format("%.1f 分（%d 条评价）", avgRating, reviewCount);
            }
        } catch (Exception e) {
            log.warn("获取教练评分统计失败, coachId={}", coach.getId(), e);
        }

        return String.format("""
                教练详情：
                - 姓名：%s
                - 专业方向：%s
                - 性别：%s
                - 年龄：%s
                - 预约价格：%s / 小时
                - 评分：%s
                - 简介：%s
                """,
                defaultText(coach.getName(), "未命名教练"),
                getSpecialtyName(coach.getSpecialty()),
                getGenderName(coach.getGender()),
                coach.getAge() == null ? "未填写" : coach.getAge() + " 岁",
                formatPrice(coach.getPrice()),
                ratingInfo,
                defaultText(coach.getIntro(), "暂无简介"));
    }

    @Tool("获取推荐教练列表")
    public String getTopRatedCoaches() {
        log.info("AI工具调用: getTopRatedCoaches");

        CoachQueryPageRequest request = new CoachQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);

        Page<CoachVo> page = coachService.listCoachPage(request);
        List<CoachVo> coaches = page.getRecords();
        if (coaches.isEmpty()) {
            return "暂无教练数据。";
        }

        StringBuilder sb = new StringBuilder("推荐教练：\n");
        for (CoachVo coach : coaches) {
            sb.append(String.format("- [%s] %s，预约价格：%s / 小时%n",
                    getSpecialtyName(coach.getSpecialty()),
                    defaultText(coach.getName(), "未命名教练"),
                    formatPrice(coach.getPrice())));
        }
        return sb.toString();
    }

    private String getSpecialtyName(String specialty) {
        if (specialty == null) {
            return "综合";
        }
        return switch (specialty) {
            case "1" -> "私人健身教练";
            case "2" -> "团体课教练";
            case "3" -> "力量训练";
            case "4" -> "瑜伽";
            case "5" -> "有氧运动";
            case "6" -> "康复/矫正训练教练";
            case "7" -> "营养与生活方式教练";
            case "8" -> "专项运动教练";
            case "9" -> "线上健身教练";
            default -> specialty;
        };
    }

    private String getGenderName(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        return gender == 1 ? "男" : "女";
    }

    private String summarizeIntro(String intro) {
        if (intro == null || intro.isBlank()) {
            return "暂无简介";
        }
        return intro.length() > 30 ? intro.substring(0, 30) + "..." : intro;
    }

    private String formatPrice(BigDecimal price) {
        return price == null ? "未设置" : price.stripTrailingZeros().toPlainString() + " 元";
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

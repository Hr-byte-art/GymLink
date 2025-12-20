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

import java.util.List;
import java.util.Map;

/**
 * 教练相关AI工具
 * @author 木子宸
 */
@Component
@Slf4j
public class CoachTools {

    @Resource
    private CoachService coachService;

    @Resource
    private CourseReviewService courseReviewService;

    @Tool("根据专业方向搜索教练。专业方向使用数字编码：1=私人健身教练,2=团体课教练,3=力量训练,4=瑜伽,5=有氧运动,6=康复训练,7=营养教练,8=专项运动,9=线上教练")
    public String searchCoaches(
            @P("教练专业方向编码，如：4表示瑜伽教练，3表示力量训练教练。不填则搜索所有教练") String specialty) {
        log.info("AI工具调用: searchCoaches, specialty={}", specialty);

        CoachQueryPageRequest request = new CoachQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);
        request.setSpecialty(specialty);

        Page<CoachVo> page = coachService.listCoachPage(request);
        List<CoachVo> coaches = page.getRecords();

        if (coaches.isEmpty()) {
            return "没有找到符合条件的教练。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到以下教练：\n");
        for (CoachVo coach : coaches) {
            sb.append(String.format("- 【%s】%s，%s，预约价格：%.2f元/小时\n",
                    getSpecialtyName(coach.getSpecialty()),
                    coach.getName(),
                    coach.getIntro() != null && coach.getIntro().length() > 30 
                        ? coach.getIntro().substring(0, 30) + "..." 
                        : (coach.getIntro() != null ? coach.getIntro() : "暂无简介"),
                    coach.getPrice() != null ? coach.getPrice() : java.math.BigDecimal.ZERO));
        }
        return sb.toString();
    }

    @Tool("根据教练姓名搜索教练")
    public String searchCoachesByName(@P("教练姓名关键词") String name) {
        log.info("AI工具调用: searchCoachesByName, name={}", name);

        CoachQueryPageRequest request = new CoachQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);
        request.setName(name);

        Page<CoachVo> page = coachService.listCoachPage(request);
        List<CoachVo> coaches = page.getRecords();

        if (coaches.isEmpty()) {
            return "没有找到名为「" + name + "」的教练。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到以下教练：\n");
        for (CoachVo coach : coaches) {
            sb.append(String.format("- 【%s】%s，预约价格：%.2f元/小时\n",
                    getSpecialtyName(coach.getSpecialty()),
                    coach.getName(),
                    coach.getPrice() != null ? coach.getPrice() : java.math.BigDecimal.ZERO));
        }
        return sb.toString();
    }

    @Tool("获取教练的详细信息，包括简介、专业方向、评价等")
    public String getCoachDetail(@P("教练姓名") String coachName) {
        log.info("AI工具调用: getCoachDetail, coachName={}", coachName);

        CoachQueryPageRequest request = new CoachQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setName(coachName);

        Page<CoachVo> page = coachService.listCoachPage(request);
        List<CoachVo> coaches = page.getRecords();

        if (coaches.isEmpty()) {
            return "没有找到名为「" + coachName + "」的教练。";
        }

        CoachVo coach = coaches.get(0);
        
        // 获取教练评价统计
        String ratingInfo = "暂无评价";
        try {
            Map<String, Object> stats = courseReviewService.getCoachReviewStats(coach.getId());
            Double avgRating = (Double) stats.get("avgRating");
            Integer reviewCount = (Integer) stats.get("reviewCount");
            if (reviewCount != null && reviewCount > 0) {
                ratingInfo = String.format("%.1f分（%d条评价）", avgRating, reviewCount);
            }
        } catch (Exception e) {
            log.warn("获取教练评价统计失败", e);
        }

        return String.format("""
                教练详情：
                - 姓名：%s
                - 专业方向：%s
                - 性别：%s
                - 年龄：%d岁
                - 预约价格：%.2f元/小时
                - 评价：%s
                - 简介：%s
                """,
                coach.getName(),
                getSpecialtyName(coach.getSpecialty()),
                coach.getGender() != null && coach.getGender() == 1 ? "男" : "女",
                coach.getAge() != null ? coach.getAge() : 0,
                coach.getPrice() != null ? coach.getPrice() : java.math.BigDecimal.ZERO,
                ratingInfo,
                coach.getIntro() != null ? coach.getIntro() : "暂无简介");
    }

    @Tool("获取高评分教练推荐")
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

        StringBuilder sb = new StringBuilder();
        sb.append("推荐教练：\n");
        for (CoachVo coach : coaches) {
            sb.append(String.format("- 【%s】%s，预约价格：%.2f元/小时\n",
                    getSpecialtyName(coach.getSpecialty()),
                    coach.getName(),
                    coach.getPrice() != null ? coach.getPrice() : java.math.BigDecimal.ZERO));
        }
        return sb.toString();
    }

    private String getSpecialtyName(String specialty) {
        if (specialty == null) return "综合";
        return switch (specialty) {
            case "1" -> "私人健身教练";
            case "2" -> "团体课教练";
            case "3" -> "力量训练";
            case "4" -> "瑜伽";
            case "5" -> "有氧运动";
            case "6" -> "康复训练";
            case "7" -> "营养教练";
            case "8" -> "专项运动";
            case "9" -> "线上教练";
            default -> specialty;
        };
    }
}

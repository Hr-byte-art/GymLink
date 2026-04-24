package com.ldr.gymlink.ai.tools;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.model.dto.course.CourseQueryPageRequest;
import com.ldr.gymlink.model.vo.CourseVo;
import com.ldr.gymlink.service.CourseService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 课程相关 AI 工具
 */
@Component
@Slf4j
public class CourseTools {

    @Resource
    private CourseService courseService;

    @Tool("根据课程类型和难度搜索健身课程。课程类型编码：1=私教课程，2=团体训练课程，3=功能性训练课程，4=力量训练课程，5=瑜伽课程，6=普拉提课程，7=康复/矫正训练课程，8=专项运动表现课程，9=孕产/产后修复课程，10=老年/青少年体适能课程，11=线上课程。")
    public String searchCourses(
            @P("课程类型编码，可为空") String type,
            @P("难度等级，可选 beginner、intermediate、advanced，也可为空") String difficulty) {
        log.info("AI工具调用: searchCourses, type={}, difficulty={}", type, difficulty);

        CourseQueryPageRequest request = new CourseQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);
        request.setType(type);
        request.setDifficulty(difficulty);

        List<CourseVo> courses = courseService.listCoursePage(request).getRecords();
        if (courses.isEmpty()) {
            return "没有找到符合条件的课程。";
        }

        StringBuilder sb = new StringBuilder("找到以下课程：\n");
        for (CourseVo course : courses) {
            sb.append(String.format("- [%s] %s，难度：%s，价格：%s，时长：%s 分钟，教练：%s%n",
                    getTypeName(course.getType()),
                    defaultText(course.getName(), "未命名课程"),
                    getDifficultyName(course.getDifficulty()),
                    formatPrice(course.getPrice()),
                    course.getDuration() == null ? "未知" : course.getDuration(),
                    defaultText(course.getCoachName(), "待定")));
        }
        return sb.toString();
    }

    @Tool("根据课程名称关键词搜索课程")
    public String searchCoursesByName(@P("课程名称关键词") String keyword) {
        log.info("AI工具调用: searchCoursesByName, keyword={}", keyword);

        CourseQueryPageRequest request = new CourseQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);
        request.setName(keyword);

        List<CourseVo> courses = courseService.listCoursePage(request).getRecords();
        if (courses.isEmpty()) {
            return "没有找到包含“" + keyword + "”的课程。";
        }

        StringBuilder sb = new StringBuilder("找到以下课程：\n");
        for (CourseVo course : courses) {
            sb.append(String.format("- [%s] %s，难度：%s，价格：%s%n",
                    getTypeName(course.getType()),
                    defaultText(course.getName(), "未命名课程"),
                    getDifficultyName(course.getDifficulty()),
                    formatPrice(course.getPrice())));
        }
        return sb.toString();
    }

    @Tool("获取课程详情，包括课程描述、难度、价格、教练等信息")
    public String getCourseDetail(@P("课程名称") String courseName) {
        log.info("AI工具调用: getCourseDetail, courseName={}", courseName);

        CourseQueryPageRequest request = new CourseQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setName(courseName);

        List<CourseVo> courses = courseService.listCoursePage(request).getRecords();
        if (courses.isEmpty()) {
            return "没有找到名为“" + courseName + "”的课程。";
        }

        CourseVo course = courses.get(0);
        return String.format("""
                课程详情：
                - 名称：%s
                - 类型：%s
                - 难度：%s
                - 价格：%s
                - 时长：%s 分钟
                - 教练：%s
                - 描述：%s
                """,
                defaultText(course.getName(), "未命名课程"),
                getTypeName(course.getType()),
                getDifficultyName(course.getDifficulty()),
                formatPrice(course.getPrice()),
                course.getDuration() == null ? "未知" : course.getDuration(),
                defaultText(course.getCoachName(), "待定"),
                defaultText(course.getDescription(), "暂无描述"));
    }

    @Tool("获取热门课程推荐")
    public String getPopularCourses() {
        log.info("AI工具调用: getPopularCourses");

        CourseQueryPageRequest request = new CourseQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);

        Page<CourseVo> page = courseService.listCoursePage(request);
        List<CourseVo> courses = page.getRecords();
        if (courses.isEmpty()) {
            return "暂无课程数据。";
        }

        StringBuilder sb = new StringBuilder("热门课程推荐：\n");
        for (CourseVo course : courses) {
            sb.append(String.format("- [%s] %s，难度：%s，价格：%s，教练：%s%n",
                    getTypeName(course.getType()),
                    defaultText(course.getName(), "未命名课程"),
                    getDifficultyName(course.getDifficulty()),
                    formatPrice(course.getPrice()),
                    defaultText(course.getCoachName(), "待定")));
        }
        return sb.toString();
    }

    private String getTypeName(String type) {
        if (type == null) {
            return "其他";
        }
        return switch (type) {
            case "1" -> "私教课程";
            case "2" -> "团体训练课程";
            case "3" -> "功能性训练课程";
            case "4" -> "力量训练课程";
            case "5" -> "瑜伽课程";
            case "6" -> "普拉提课程";
            case "7" -> "康复/矫正训练课程";
            case "8" -> "专项运动表现课程";
            case "9" -> "孕产/产后修复课程";
            case "10" -> "老年/青少年体适能课程";
            case "11" -> "线上课程";
            default -> type;
        };
    }

    private String getDifficultyName(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            return "未设置";
        }
        return switch (difficulty.toLowerCase()) {
            case "beginner" -> "初级";
            case "intermediate" -> "中级";
            case "advanced" -> "高级";
            default -> difficulty;
        };
    }

    private String formatPrice(BigDecimal price) {
        return price == null ? "未设置" : price.stripTrailingZeros().toPlainString() + " 元";
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

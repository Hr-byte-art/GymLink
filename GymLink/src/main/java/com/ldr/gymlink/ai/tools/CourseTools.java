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

import java.util.List;

/**
 * 课程相关AI工具
 * @author 木子宸
 */
@Component
@Slf4j
public class CourseTools {

    @Resource
    private CourseService courseService;

    @Tool("根据课程类型和难度搜索健身课程。类型使用数字编码：1=私教课程,2=团体训练,3=功能性训练,4=力量训练,5=瑜伽,6=普拉提,7=康复训练,8=专项运动,9=孕产修复,10=老年青少年体适能,11=线上课程")
    public String searchCourses(
            @P("课程类型编码，如：5表示瑜伽课程，4表示力量训练课程。不填则搜索所有类型") String type,
            @P("难度等级，可选值：初级、中级、高级。不填则搜索所有难度") String difficulty) {
        log.info("AI工具调用: searchCourses, type={}, difficulty={}", type, difficulty);

        CourseQueryPageRequest request = new CourseQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);
        request.setType(type);
        request.setDifficulty(difficulty);

        Page<CourseVo> page = courseService.listCoursePage(request);
        List<CourseVo> courses = page.getRecords();

        if (courses.isEmpty()) {
            return "没有找到符合条件的课程。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到以下课程：\n");
        for (CourseVo course : courses) {
            sb.append(String.format("- 【%s】%s，难度：%s，价格：%.2f元，时长：%d分钟，教练：%s\n",
                    getTypeName(course.getType()),
                    course.getName(),
                    course.getDifficulty() != null ? course.getDifficulty() : "未知",
                    course.getPrice() != null ? course.getPrice() : java.math.BigDecimal.ZERO,
                    course.getDuration() != null ? course.getDuration() : 0,
                    course.getCoachName() != null ? course.getCoachName() : "待定"));
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

        Page<CourseVo> page = courseService.listCoursePage(request);
        List<CourseVo> courses = page.getRecords();

        if (courses.isEmpty()) {
            return "没有找到包含「" + keyword + "」的课程。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到以下课程：\n");
        for (CourseVo course : courses) {
            sb.append(String.format("- 【%s】%s，难度：%s，价格：%.2f元\n",
                    getTypeName(course.getType()),
                    course.getName(),
                    course.getDifficulty() != null ? course.getDifficulty() : "未知",
                    course.getPrice() != null ? course.getPrice() : java.math.BigDecimal.ZERO));
        }
        return sb.toString();
    }

    @Tool("获取课程的详细信息，包括课程描述、教练信息等")
    public String getCourseDetail(@P("课程名称") String courseName) {
        log.info("AI工具调用: getCourseDetail, courseName={}", courseName);

        CourseQueryPageRequest request = new CourseQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setName(courseName);

        Page<CourseVo> page = courseService.listCoursePage(request);
        List<CourseVo> courses = page.getRecords();

        if (courses.isEmpty()) {
            return "没有找到名为「" + courseName + "」的课程。";
        }

        CourseVo course = courses.get(0);
        return String.format("""
                课程详情：
                - 名称：%s
                - 类型：%s
                - 难度：%s
                - 价格：%.2f元
                - 时长：%d分钟
                - 教练：%s
                - 描述：%s
                """,
                course.getName(),
                getTypeName(course.getType()),
                course.getDifficulty() != null ? course.getDifficulty() : "未知",
                course.getPrice() != null ? course.getPrice() : java.math.BigDecimal.ZERO,
                course.getDuration() != null ? course.getDuration() : 0,
                course.getCoachName() != null ? course.getCoachName() : "待定",
                course.getDescription() != null ? course.getDescription() : "暂无描述");
    }

    @Tool("获取热门课程推荐，按购买人数排序")
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

        StringBuilder sb = new StringBuilder();
        sb.append("热门课程推荐：\n");
        for (CourseVo course : courses) {
            sb.append(String.format("- 【%s】%s，难度：%s，价格：%.2f元，教练：%s\n",
                    getTypeName(course.getType()),
                    course.getName(),
                    course.getDifficulty() != null ? course.getDifficulty() : "未知",
                    course.getPrice() != null ? course.getPrice() : java.math.BigDecimal.ZERO,
                    course.getCoachName() != null ? course.getCoachName() : "待定"));
        }
        return sb.toString();
    }

    private String getTypeName(String type) {
        if (type == null) return "其他";
        return switch (type) {
            case "1" -> "私教课程";
            case "2" -> "团体训练";
            case "3" -> "功能性训练";
            case "4" -> "力量训练";
            case "5" -> "瑜伽";
            case "6" -> "普拉提";
            case "7" -> "康复训练";
            case "8" -> "专项运动";
            case "9" -> "孕产修复";
            case "10" -> "老年青少年体适能";
            case "11" -> "线上课程";
            default -> type;
        };
    }
}

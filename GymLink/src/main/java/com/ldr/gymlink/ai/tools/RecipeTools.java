package com.ldr.gymlink.ai.tools;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.model.dto.recipe.RecipeQueryPageRequest;
import com.ldr.gymlink.model.vo.RecipeVo;
import com.ldr.gymlink.service.RecipeService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 食谱相关AI工具
 */
@Component
@Slf4j
public class RecipeTools {

    @Resource
    private RecipeService recipeService;

    @Tool("根据食谱标签搜索健身食谱。标签编码：1=增肌食谱,2=减脂食谱,3=维持期食谱,4=高蛋白食谱,5=低碳/生酮,6=力量训练专用,7=耐力训练专用,8=素食健身,9=清单饮食,10=周期化食谱")
    public String searchRecipes(
            @P("食谱标签编码，如：1表示增肌食谱，2表示减脂食谱。不填则搜索所有食谱") String tag) {
        log.info("AI工具调用: searchRecipes, tag={}", tag);

        RecipeQueryPageRequest request = new RecipeQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);
        request.setTags(tag);

        Page<RecipeVo> page = recipeService.listRecipePage(request);
        List<RecipeVo> recipes = page.getRecords();

        if (recipes.isEmpty()) {
            return "没有找到符合条件的食谱。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到以下食谱：\n");
        for (RecipeVo recipe : recipes) {
            sb.append(String.format("- 【%s】%s\n",
                    getTagNames(recipe.getTags()),
                    recipe.getTitle()));
        }
        return sb.toString();
    }

    @Tool("根据食谱标题关键词搜索食谱")
    public String searchRecipesByTitle(@P("食谱标题关键词") String keyword) {
        log.info("AI工具调用: searchRecipesByTitle, keyword={}", keyword);

        RecipeQueryPageRequest request = new RecipeQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);
        request.setTitle(keyword);

        Page<RecipeVo> page = recipeService.listRecipePage(request);
        List<RecipeVo> recipes = page.getRecords();

        if (recipes.isEmpty()) {
            return "没有找到包含「" + keyword + "」的食谱。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到以下食谱：\n");
        for (RecipeVo recipe : recipes) {
            sb.append(String.format("- 【%s】%s\n",
                    getTagNames(recipe.getTags()),
                    recipe.getTitle()));
        }
        return sb.toString();
    }

    @Tool("获取食谱的详细信息，包括内容详情等")
    public String getRecipeDetail(@P("食谱名称/标题") String recipeName) {
        log.info("AI工具调用: getRecipeDetail, recipeName={}", recipeName);

        RecipeQueryPageRequest request = new RecipeQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setTitle(recipeName);

        Page<RecipeVo> page = recipeService.listRecipePage(request);
        List<RecipeVo> recipes = page.getRecords();

        if (recipes.isEmpty()) {
            return "没有找到名为「" + recipeName + "」的食谱。";
        }

        RecipeVo recipe = recipes.get(0);
        // 简化内容，去除HTML标签
        String content = recipe.getContent() != null ?
                recipe.getContent().replaceAll("<[^>]*>", "") : "暂无内容";
        if (content.length() > 300) {
            content = content.substring(0, 300) + "...";
        }

        return String.format("""
                食谱详情：
                - 标题：%s
                - 标签：%s
                - 内容摘要：%s
                """,
                recipe.getTitle(),
                getTagNames(recipe.getTags()),
                content);
    }

    @Tool("根据健身目标推荐合适的食谱")
    public String getRecipeRecommendation(
            @P("健身目标，可选值：增肌、减脂、维持、力量训练、耐力训练") String goal) {
        log.info("AI工具调用: getRecipeRecommendation, goal={}", goal);

        String tagCode;
        String tips;

        switch (goal) {
            case "增肌" -> {
                tagCode = "1";
                tips = "增肌期建议：高热量、高蛋白（每公斤体重1.6-2.2g蛋白质），适量碳水，支持肌肉合成与训练恢复。";
            }
            case "减脂" -> {
                tagCode = "2";
                tips = "减脂期建议：保持热量缺口，高蛋白、中低碳水，多吃蔬菜，保留肌肉同时减少体脂。";
            }
            case "维持" -> {
                tagCode = "3";
                tips = "维持期建议：热量平衡，营养均衡，用于体重/体脂稳定阶段。";
            }
            case "力量训练" -> {
                tagCode = "6";
                tips = "力量训练建议：强调训练前后营养时机，练前补充碳水，练后补充蛋白质，提升表现与恢复。";
            }
            case "耐力训练" -> {
                tagCode = "7";
                tips = "耐力训练建议：高碳水储备，支持长时间有氧运动，注意补充电解质。";
            }
            default -> {
                tagCode = "4";
                tips = "通用建议：保证充足蛋白质摄入，均衡饮食，多喝水。";
            }
        }

        // 查询推荐的食谱
        RecipeQueryPageRequest request = new RecipeQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(3);
        request.setTags(tagCode);

        Page<RecipeVo> page = recipeService.listRecipePage(request);
        List<RecipeVo> recipes = page.getRecords();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("根据您的健身目标「%s」，推荐以下食谱：\n\n", goal));
        sb.append("💡 ").append(tips).append("\n\n");

        if (recipes.isEmpty()) {
            sb.append("暂无该类型的食谱，建议查看其他分类。");
        } else {
            sb.append("推荐食谱：\n");
            for (RecipeVo recipe : recipes) {
                sb.append(String.format("- %s\n", recipe.getTitle()));
            }
        }
        return sb.toString();
    }

    @Tool("获取热门食谱推荐")
    public String getPopularRecipes() {
        log.info("AI工具调用: getPopularRecipes");

        RecipeQueryPageRequest request = new RecipeQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);

        Page<RecipeVo> page = recipeService.listRecipePage(request);
        List<RecipeVo> recipes = page.getRecords();

        if (recipes.isEmpty()) {
            return "暂无食谱数据。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("热门食谱推荐：\n");
        for (RecipeVo recipe : recipes) {
            sb.append(String.format("- 【%s】%s\n",
                    getTagNames(recipe.getTags()),
                    recipe.getTitle()));
        }
        return sb.toString();
    }

    private String getTagNames(String tags) {
        if (tags == null || tags.isEmpty()) return "其他";
        
        StringBuilder sb = new StringBuilder();
        String[] tagArray = tags.split(",");
        for (int i = 0; i < tagArray.length; i++) {
            if (i > 0) sb.append("、");
            sb.append(getTagName(tagArray[i].trim()));
        }
        return sb.toString();
    }

    private String getTagName(String tag) {
        return switch (tag) {
            case "1" -> "增肌食谱";
            case "2" -> "减脂食谱";
            case "3" -> "维持期食谱";
            case "4" -> "高蛋白食谱";
            case "5" -> "低碳/生酮";
            case "6" -> "力量训练专用";
            case "7" -> "耐力训练专用";
            case "8" -> "素食健身";
            case "9" -> "清单饮食";
            case "10" -> "周期化食谱";
            default -> tag;
        };
    }
}

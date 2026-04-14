package com.ldr.gymlink.ai.aiService;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * @author 木子宸
 * @Description 食谱营养分析 AI 服务
 */
public interface RecipeNutritionAnalyzer {

    @SystemMessage(fromResource = "prompt/analyze-nutrition-prompt.md")
    String analyzeNutrition(@UserMessage String recipeInfo);
}

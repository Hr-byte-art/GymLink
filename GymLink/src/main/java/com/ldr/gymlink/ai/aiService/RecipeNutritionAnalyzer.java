package com.ldr.gymlink.ai.aiService;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * @author 木子宸
 * @Description 食谱营养分析 AI 服务
 */
public interface RecipeNutritionAnalyzer {

    @SystemMessage("""
            你是一个专业的营养分析师。根据用户提供的食谱标题和内容，分析并估算该食谱的营养信息。
            
            请严格按照以下JSON格式返回结果，不要包含任何其他文字：
            {
                "calories": 数字(热量，单位千卡，整数),
                "protein": 数字(蛋白质，单位克，整数),
                "carbs": 数字(碳水化合物，单位克，整数),
                "fat": 数字(脂肪，单位克，整数),
                "prepTime": 数字(准备时间，单位分钟，整数),
                "cookTime": 数字(烹饪时间，单位分钟，整数),
                "servings": 数字(份数，整数，默认1),
                "difficulty": "字符串(难度：easy/medium/hard)"
            }
            
            分析要点：
            1. 根据食材估算热量和营养成分（每份）
            2. 根据烹饪步骤估算准备和烹饪时间
            3. 根据步骤复杂度判断难度
            4. 如果信息不足，给出合理的估算值
            
            只返回JSON，不要有任何解释文字。
            """)
    String analyzeNutrition(@UserMessage String recipeInfo);
}

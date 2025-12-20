package com.ldr.gymlink.model.dto.recipe;

import lombok.Data;

/**
 * @Description AI营养分析结果
 */
@Data
public class NutritionAnalysisResult {
    
    /**
     * 热量(千卡)
     */
    private Integer calories;
    
    /**
     * 蛋白质(克)
     */
    private Integer protein;
    
    /**
     * 碳水化合物(克)
     */
    private Integer carbs;
    
    /**
     * 脂肪(克)
     */
    private Integer fat;
    
    /**
     * 准备时间(分钟)
     */
    private Integer prepTime;
    
    /**
     * 烹饪时间(分钟)
     */
    private Integer cookTime;
    
    /**
     * 份数
     */
    private Integer servings;
    
    /**
     * 难度(easy/medium/hard)
     */
    private String difficulty;
}

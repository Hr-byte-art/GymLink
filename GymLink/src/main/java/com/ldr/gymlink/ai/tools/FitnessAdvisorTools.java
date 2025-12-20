package com.ldr.gymlink.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 健身建议AI工具
 */
@Component
@Slf4j
public class FitnessAdvisorTools {

    @Tool("根据用户的健身目标提供综合建议，包括推荐的课程类型、食谱分类和训练建议")
    public String getFitnessRecommendation(
            @P("健身目标，可选值：增肌、减脂、塑形、提高体能、康复训练") String fitnessGoal) {
        log.info("AI工具调用: getFitnessRecommendation, fitnessGoal={}", fitnessGoal);

        String courseRecommendation;
        String recipeRecommendation;
        String equipmentRecommendation;
        String tips;

        switch (fitnessGoal) {
            case "增肌" -> {
                courseRecommendation = "力量训练课程(4)、私教课程(1)";
                recipeRecommendation = "增肌食谱(1)、高蛋白食谱(4)";
                equipmentRecommendation = "力量训练器材(2)、自由重量器材(2-2)";
                tips = "建议每周进行3-4次力量训练，每次45-60分钟。注意补充足够的蛋白质（每公斤体重1.6-2.2g），保证充足睡眠促进肌肉恢复。";
            }
            case "减脂" -> {
                courseRecommendation = "团体训练课程(2)、有氧运动相关课程";
                recipeRecommendation = "减脂食谱(2)、低碳/生酮食谱(5)";
                equipmentRecommendation = "有氧健身器材(1)、跑步机(1-1)、动感单车(1-3)";
                tips = "建议每周进行4-5次有氧运动，每次30-45分钟。保持适度热量缺口（每天减少300-500卡），多喝水，避免高糖高油食物。";
            }
            case "塑形" -> {
                courseRecommendation = "瑜伽课程(5)、普拉提课程(6)、功能性训练课程(3)";
                recipeRecommendation = "维持期食谱(3)、高蛋白食谱(4)";
                equipmentRecommendation = "功能性训练器材(3)、小型健身器械(4)";
                tips = "建议有氧和力量训练结合，注重核心训练和体态矫正。每周3-4次训练，配合拉伸放松。";
            }
            case "提高体能" -> {
                courseRecommendation = "功能性训练课程(3)、专项运动表现课程(8)";
                recipeRecommendation = "高蛋白食谱(4)、耐力训练专用(7)";
                equipmentRecommendation = "功能性训练器材(3)、有氧健身器材(1)";
                tips = "建议进行综合性训练，包括力量、耐力、爆发力、柔韧性等多方面训练。循序渐进，避免过度训练。";
            }
            case "康复训练" -> {
                courseRecommendation = "康复/矫正训练课程(7)、瑜伽课程(5)、普拉提课程(6)";
                recipeRecommendation = "维持期食谱(3)、清单饮食(9)";
                equipmentRecommendation = "康复与辅助器材(5)";
                tips = "建议在专业教练指导下进行，循序渐进，避免过度训练。注意倾听身体信号，如有不适及时停止。";
            }
            default -> {
                courseRecommendation = "功能性训练课程(3)、团体训练课程(2)";
                recipeRecommendation = "维持期食谱(3)";
                equipmentRecommendation = "有氧健身器材(1)、功能性训练器材(3)";
                tips = "建议先进行体能评估，制定个性化训练计划。初学者可以从基础课程开始，逐步提升强度。";
            }
        }

        return String.format("""
                根据您的健身目标「%s」，为您提供以下建议：
                
                📚 推荐课程类型：%s
                🍽️ 推荐食谱分类：%s
                🏋️ 推荐器材类型：%s
                
                💡 训练建议：%s
                
                您可以让我帮您搜索具体的课程、食谱或器材！
                """, fitnessGoal, courseRecommendation, recipeRecommendation, equipmentRecommendation, tips);
    }

    @Tool("根据用户的身体数据计算BMI并给出健身建议")
    public String calculateBMIAndAdvice(
            @P("身高，单位：厘米") double heightCm,
            @P("体重，单位：公斤") double weightKg) {
        log.info("AI工具调用: calculateBMIAndAdvice, height={}, weight={}", heightCm, weightKg);

        double heightM = heightCm / 100;
        double bmi = weightKg / (heightM * heightM);
        bmi = Math.round(bmi * 10) / 10.0;

        String category;
        String advice;

        if (bmi < 18.5) {
            category = "偏瘦";
            advice = "建议增加热量摄入，进行力量训练增肌。推荐增肌食谱和力量训练课程。";
        } else if (bmi < 24) {
            category = "正常";
            advice = "体重在健康范围内，可以根据个人目标选择塑形或提高体能的训练计划。";
        } else if (bmi < 28) {
            category = "偏胖";
            advice = "建议控制饮食热量，增加有氧运动。推荐减脂食谱和有氧训练课程。";
        } else {
            category = "肥胖";
            advice = "建议在专业指导下进行减脂计划，从低强度有氧开始，逐步增加运动量。注意饮食控制。";
        }

        return String.format("""
                BMI计算结果：
                - 身高：%.1f cm
                - 体重：%.1f kg
                - BMI指数：%.1f
                - 体重分类：%s
                
                💡 健身建议：%s
                
                注：BMI仅供参考，实际健身计划还需考虑体脂率、肌肉量等因素。
                """, heightCm, weightKg, bmi, category, advice);
    }

    @Tool("获取新手入门健身指南")
    public String getBeginnerGuide() {
        log.info("AI工具调用: getBeginnerGuide");

        return """
                🏋️ 新手健身入门指南
                
                一、开始前的准备
                1. 进行体能评估，了解自己的身体状况
                2. 设定明确的健身目标（增肌/减脂/塑形/提高体能）
                3. 准备合适的运动装备和运动鞋
                
                二、推荐的入门课程
                - 功能性训练课程（初级）：增强核心力量，改善体态
                - 瑜伽课程（初级）：提升柔韧性，学习正确呼吸
                - 团体训练课程（初级）：提升心肺功能，燃烧脂肪
                
                三、训练频率建议
                - 初期：每周2-3次，每次30-45分钟
                - 适应后：每周3-4次，每次45-60分钟
                - 注意休息，避免连续训练同一肌群
                
                四、饮食建议
                - 保证充足蛋白质摄入
                - 训练前1-2小时适量进食
                - 训练后30分钟内补充蛋白质
                - 多喝水，每天至少2升
                
                五、注意事项
                - 热身5-10分钟再开始训练
                - 动作标准比重量更重要
                - 循序渐进，不要急于求成
                - 如有不适，及时停止并咨询教练
                
                有任何问题可以随时问我！💪
                """;
    }

    @Tool("获取健身常见问题解答")
    public String getFitnessFAQ(@P("问题类型，可选值：训练、饮食、恢复、器材使用") String questionType) {
        log.info("AI工具调用: getFitnessFAQ, questionType={}", questionType);

        return switch (questionType) {
            case "训练" -> """
                    🏋️ 训练常见问题
                    
                    Q: 一周应该训练几次？
                    A: 初学者建议2-3次，有基础后可以增加到4-5次。注意给肌肉足够的恢复时间。
                    
                    Q: 有氧和力量训练哪个先做？
                    A: 如果目标是增肌，先做力量训练；如果目标是减脂，可以先做有氧或交替进行。
                    
                    Q: 训练多久能看到效果？
                    A: 一般坚持4-6周可以感受到体能提升，8-12周可以看到明显的身体变化。
                    
                    Q: 肌肉酸痛还能继续训练吗？
                    A: 轻微酸痛可以训练其他部位，严重酸痛建议休息1-2天。
                    """;
            case "饮食" -> """
                    🍽️ 饮食常见问题
                    
                    Q: 训练前应该吃什么？
                    A: 训练前1-2小时吃易消化的碳水化合物，如香蕉、燕麦等。
                    
                    Q: 训练后应该吃什么？
                    A: 训练后30分钟内补充蛋白质和碳水，如蛋白粉、鸡胸肉配米饭等。
                    
                    Q: 减脂期间可以吃碳水吗？
                    A: 可以，但要控制量和选择优质碳水（如糙米、燕麦），避免精制碳水。
                    
                    Q: 每天需要喝多少水？
                    A: 建议每天至少2升，训练时额外补充500ml-1L。
                    """;
            case "恢复" -> """
                    😴 恢复常见问题
                    
                    Q: 为什么恢复很重要？
                    A: 肌肉在休息时生长，充足的恢复能提高训练效果，预防受伤。
                    
                    Q: 如何加速恢复？
                    A: 保证7-8小时睡眠、补充蛋白质、适当拉伸、可以尝试泡沫轴放松。
                    
                    Q: 训练后需要拉伸吗？
                    A: 建议训练后进行5-10分钟静态拉伸，有助于肌肉恢复和柔韧性。
                    
                    Q: 肌肉酸痛怎么缓解？
                    A: 轻度活动、热敷、按摩、充足睡眠都有帮助。
                    """;
            case "器材使用" -> """
                    🏋️ 器材使用常见问题
                    
                    Q: 新手应该用固定器械还是自由重量？
                    A: 建议先从固定器械开始，掌握动作模式后再尝试自由重量。
                    
                    Q: 跑步机和椭圆机哪个更好？
                    A: 椭圆机对关节冲击更小，适合体重较大或关节不好的人；跑步机更接近自然跑步。
                    
                    Q: 如何选择合适的重量？
                    A: 选择能完成8-12次、最后几次感到吃力的重量。
                    
                    Q: 器材使用前需要注意什么？
                    A: 检查器材状态、调整到合适位置、先用轻重量热身。
                    """;
            default -> """
                    请选择具体的问题类型：
                    - 训练：关于训练频率、方法、效果等
                    - 饮食：关于训练前后饮食、营养搭配等
                    - 恢复：关于休息、拉伸、肌肉恢复等
                    - 器材使用：关于各类健身器材的使用方法
                    """;
        };
    }
}

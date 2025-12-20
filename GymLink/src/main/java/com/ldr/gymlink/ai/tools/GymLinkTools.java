package com.ldr.gymlink.ai.tools;

import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * GymLink AI 工具集聚合类
 * 聚合所有分类工具，提供给 AI 助手调用
 * @author 木子宸
 */
@Component
@Slf4j
@Getter
public class GymLinkTools {

    @Resource
    private CourseTools courseTools;

    @Resource
    private CoachTools coachTools;

    @Resource
    private EquipmentTools equipmentTools;

    @Resource
    private RecipeTools recipeTools;

    @Resource
    private FitnessAdvisorTools fitnessAdvisorTools;

    /**
     * 获取所有工具对象列表，用于注册到AI服务
     */
    public List<Object> getAllTools() {
        return Arrays.asList(
                courseTools,
                coachTools,
                equipmentTools,
                recipeTools,
                fitnessAdvisorTools
        );
    }
}

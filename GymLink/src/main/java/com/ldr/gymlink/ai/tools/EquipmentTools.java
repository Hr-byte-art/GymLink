package com.ldr.gymlink.ai.tools;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.model.dto.equipment.EquipmentQueryPageRequest;
import com.ldr.gymlink.model.vo.EquipmentVo;
import com.ldr.gymlink.service.EquipmentService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 器材相关AI工具
 * @author 木子宸
 */
@Component
@Slf4j
public class EquipmentTools {

    @Resource
    private EquipmentService equipmentService;

    @Tool("根据器材类型搜索健身器材。类型编码：1=有氧健身器材,1-1=跑步机,1-2=椭圆机,1-3=动感单车,1-4=划船机,2=力量训练器材,2-1=固定器械,2-2=自由重量,3=功能性训练器材,4=小型健身器械,5=康复辅助器材")
    public String searchEquipments(
            @P("器材类型编码，如：1表示有氧健身器材，1-1表示跑步机，2表示力量训练器材。不填则搜索所有器材") String type) {
        log.info("AI工具调用: searchEquipments, type={}", type);

        EquipmentQueryPageRequest request = new EquipmentQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);
        request.setType(type);

        Page<EquipmentVo> page = equipmentService.listEquipmentPage(request);
        List<EquipmentVo> equipments = page.getRecords();

        if (equipments.isEmpty()) {
            return "没有找到符合条件的器材。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到以下器材：\n");
        for (EquipmentVo equipment : equipments) {
            String statusText = equipment.getStatus() != null && equipment.getStatus() == 1 ? "可用" : "维护中";
            sb.append(String.format("- 【%s】%s，位置：%s，状态：%s，数量：%d\n",
                    getTypeName(equipment.getType()),
                    equipment.getName(),
                    equipment.getLocation() != null ? equipment.getLocation() : "未知",
                    statusText,
                    equipment.getTotalCount() != null ? equipment.getTotalCount() : 0));
        }
        return sb.toString();
    }

    @Tool("根据器材名称关键词搜索器材")
    public String searchEquipmentsByName(@P("器材名称关键词") String keyword) {
        log.info("AI工具调用: searchEquipmentsByName, keyword={}", keyword);

        EquipmentQueryPageRequest request = new EquipmentQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);
        request.setName(keyword);

        Page<EquipmentVo> page = equipmentService.listEquipmentPage(request);
        List<EquipmentVo> equipments = page.getRecords();

        if (equipments.isEmpty()) {
            return "没有找到包含「" + keyword + "」的器材。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到以下器材：\n");
        for (EquipmentVo equipment : equipments) {
            String statusText = equipment.getStatus() != null && equipment.getStatus() == 1 ? "可用" : "维护中";
            sb.append(String.format("- 【%s】%s，状态：%s\n",
                    getTypeName(equipment.getType()),
                    equipment.getName(),
                    statusText));
        }
        return sb.toString();
    }

    @Tool("获取器材的详细信息和使用说明")
    public String getEquipmentDetail(@P("器材名称") String equipmentName) {
        log.info("AI工具调用: getEquipmentDetail, equipmentName={}", equipmentName);

        EquipmentQueryPageRequest request = new EquipmentQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setName(equipmentName);

        Page<EquipmentVo> page = equipmentService.listEquipmentPage(request);
        List<EquipmentVo> equipments = page.getRecords();

        if (equipments.isEmpty()) {
            return "没有找到名为「" + equipmentName + "」的器材。";
        }

        EquipmentVo equipment = equipments.get(0);
        String statusText = equipment.getStatus() != null && equipment.getStatus() == 1 ? "正常可用" : "维护中";
        return String.format("""
                器材详情：
                - 名称：%s
                - 类型：%s
                - 位置：%s
                - 状态：%s
                - 总数量：%d台
                - 使用说明：%s
                """,
                equipment.getName(),
                getTypeName(equipment.getType()),
                equipment.getLocation() != null ? equipment.getLocation() : "未知",
                statusText,
                equipment.getTotalCount() != null ? equipment.getTotalCount() : 0,
                equipment.getDescription() != null ? equipment.getDescription() : "暂无使用说明");
    }


    @Tool("获取器材使用建议，根据训练目标推荐合适的器材")
    public String getEquipmentRecommendation(
            @P("训练目标，可选值：增肌、减脂、有氧、力量、康复") String goal) {
        log.info("AI工具调用: getEquipmentRecommendation, goal={}", goal);

        String recommendation;
        String typeCode;

        switch (goal) {
            case "增肌", "力量" -> {
                recommendation = "力量训练器材";
                typeCode = "2";
            }
            case "减脂", "有氧" -> {
                recommendation = "有氧健身器材";
                typeCode = "1";
            }
            case "康复" -> {
                recommendation = "康复与辅助器材";
                typeCode = "5";
            }
            default -> {
                recommendation = "功能性训练器材";
                typeCode = "3";
            }
        }

        // 查询推荐类型的器材
        EquipmentQueryPageRequest request = new EquipmentQueryPageRequest();
        request.setPageNum(1);
        request.setPageSize(5);
        request.setType(typeCode);
        request.setStatus(1);

        Page<EquipmentVo> page = equipmentService.listEquipmentPage(request);
        List<EquipmentVo> equipments = page.getRecords();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("根据您的训练目标「%s」，推荐使用%s：\n\n", goal, recommendation));

        if (equipments.isEmpty()) {
            sb.append("暂无该类型的可用器材。");
        } else {
            for (EquipmentVo equipment : equipments) {
                sb.append(String.format("- %s，位置：%s\n",
                        equipment.getName(),
                        equipment.getLocation() != null ? equipment.getLocation() : "未知"));
            }
        }
        return sb.toString();
    }

    private String getTypeName(String type) {
        if (type == null) return "其他";
        return switch (type) {
            case "1" -> "有氧健身器材";
            case "1-1" -> "跑步机";
            case "1-2" -> "椭圆机";
            case "1-3" -> "动感单车";
            case "1-4" -> "划船机";
            case "1-5" -> "健身车";
            case "1-6" -> "楼梯机";
            case "1-7" -> "体适能运动机";
            case "2" -> "力量训练器材";
            case "2-1" -> "固定器械";
            case "2-2" -> "自由重量器材";
            case "2-3" -> "综合训练器材";
            case "3" -> "功能性训练器材";
            case "4" -> "小型健身器械";
            case "5" -> "康复与辅助器材";
            case "6" -> "其他辅助设备";
            case "7" -> "商用专用器材";
            case "8" -> "家用专用器材";
            default -> type;
        };
    }
}

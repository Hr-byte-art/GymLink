package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.model.dto.coach.AddCoachRequest;
import com.ldr.gymlink.model.dto.coach.CoachQueryPageRequest;
import com.ldr.gymlink.model.dto.coach.UpdateCoachRequest;
import com.ldr.gymlink.model.vo.CoachVo;
import com.ldr.gymlink.service.CoachService;
import com.ldr.gymlink.utils.ResultUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 00:32:02
 * @Description
 */
@RestController
@RequestMapping("/coach")
@Tag(name = "教练管理")
public class CoachController {
    @Resource
    private CoachService coachService;

    @PostMapping("/addCoach")
    @Tag(name = "添加教练")
    public BaseResponse<CoachVo> addCoach(@RequestBody AddCoachRequest addCoachRequest) {
        CoachVo coachVo = coachService.addCoach(addCoachRequest);
        return ResultUtils.success(coachVo);
    }

    @GetMapping("/getCoachByUserId")
    @Tag(name = "通过用户id获取教练信息")
    public BaseResponse<CoachVo> getCoachByUserId(@RequestParam Long userId) {
        CoachVo coachVo = coachService.getCoachByUserId(userId);
        return ResultUtils.success(coachVo);
    }

    @PostMapping("/ListCoach")
    @Tag(name = "获取教练列表")
    public BaseResponse<Page<CoachVo>> listCoach(
            @RequestBody CoachQueryPageRequest coachQueryPageRequest) {
        Page<CoachVo> coachVos = coachService.listCoachPage(coachQueryPageRequest);
        return ResultUtils.success(coachVos);
    }

    @PostMapping("/deleteCoach")
    @Tag(name = "删除教练")
    public BaseResponse<Boolean> deleteCoach(@RequestParam Long id) {
        boolean delete = coachService.deleteCoach(id);
        return ResultUtils.success(delete);
    }

    @PostMapping("/updateCoach")
    @Tag(name = "更新教练信息")
    public BaseResponse<Boolean> updateCoach(
            @RequestParam Long id,
            @RequestBody UpdateCoachRequest updateCoachRequest) {
        boolean update = coachService.updateCoach(id, updateCoachRequest);
        return ResultUtils.success(update);
    }

    @GetMapping("/getCoachById")
    @Tag(name = "通过id获取教练信息")
    public BaseResponse<CoachVo> getCoachById(@RequestParam Long id) {
        CoachVo coachVo = coachService.getCoachById(id);
        return ResultUtils.success(coachVo);
    }

}

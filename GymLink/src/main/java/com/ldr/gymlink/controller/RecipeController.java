package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.model.dto.recipe.AddRecipeRequest;
import com.ldr.gymlink.model.dto.recipe.RecipeQueryPageRequest;
import com.ldr.gymlink.model.dto.recipe.UpdateRecipeRequest;
import com.ldr.gymlink.model.vo.RecipeVo;
import com.ldr.gymlink.service.RecipeService;
import com.ldr.gymlink.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @Description 菜谱管理
 */
@RestController
@RequestMapping("/recipe")
@Tag(name = "菜谱管理")
public class RecipeController {

    @Resource
    private RecipeService recipeService;

    @PostMapping("/addRecipe")
    @Operation(summary = "添加菜谱")
    public BaseResponse<RecipeVo> addRecipe(@RequestBody AddRecipeRequest addRecipeRequest) {
        RecipeVo recipeVo = recipeService.addRecipe(addRecipeRequest);
        return ResultUtils.success(recipeVo);
    }

    @PostMapping("/deleteRecipe")
    @Operation(summary = "删除菜谱")
    public BaseResponse<Boolean> deleteRecipe(@RequestParam Long id) {
        boolean delete = recipeService.deleteRecipe(id);
        return ResultUtils.success(delete);
    }

    @PostMapping("/updateRecipe")
    @Operation(summary = "更新菜谱")
    public BaseResponse<Boolean> updateRecipe(
            @RequestParam Long id,
            @RequestBody UpdateRecipeRequest updateRecipeRequest) {
        boolean update = recipeService.updateRecipe(id, updateRecipeRequest);
        return ResultUtils.success(update);
    }

    @GetMapping("/getRecipeById")
    @Operation(summary = "根据id获取菜谱")
    public BaseResponse<RecipeVo> getRecipeById(@RequestParam Long id) {
        RecipeVo recipeVo = recipeService.getRecipeById(id);
        return ResultUtils.success(recipeVo);
    }

    @PostMapping("/listRecipe")
    @Operation(summary = "分页查询菜谱")
    public BaseResponse<Page<RecipeVo>> listRecipe(
            @RequestBody RecipeQueryPageRequest recipeQueryPageRequest) {
        Page<RecipeVo> recipeVoPage = recipeService.listRecipePage(recipeQueryPageRequest);
        return ResultUtils.success(recipeVoPage);
    }

    @PostMapping("/updateRecipeImage")
    @Operation(summary = "修改菜谱封面图片")
    public BaseResponse<String> updateRecipeImage(@RequestParam Long recipeId, @RequestParam org.springframework.web.multipart.MultipartFile image) {
        if (recipeId == null) {
            throw new com.ldr.gymlink.exception.BusinessException(com.ldr.gymlink.exception.ErrorCode.PARAMS_ERROR, "菜谱id不能为空");
        }
        String imageUrl = recipeService.updateRecipeImage(recipeId, image);
        return ResultUtils.success(imageUrl);
    }
}

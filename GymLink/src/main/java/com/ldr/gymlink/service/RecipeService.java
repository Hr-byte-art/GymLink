package com.ldr.gymlink.service;

import com.ldr.gymlink.model.entity.Recipe;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.recipe.AddRecipeRequest;
import com.ldr.gymlink.model.dto.recipe.RecipeQueryPageRequest;
import com.ldr.gymlink.model.dto.recipe.UpdateRecipeRequest;
import com.ldr.gymlink.model.vo.RecipeVo;

/**
 * @author 木子宸
 * @description 针对表【recipe(健身菜谱表)】的数据库操作Service
 * @createDate 2025-12-01 21:36:28
 */
public interface RecipeService extends IService<Recipe> {

    /**
     * 添加菜谱
     *
     * @param addRecipeRequest
     * @return
     */
    RecipeVo addRecipe(AddRecipeRequest addRecipeRequest);

    /**
     * 删除菜谱
     *
     * @param id
     * @return
     */
    boolean deleteRecipe(Long id);

    /**
     * 更新菜谱
     *
     * @param id
     * @param updateRecipeRequest
     * @return
     */
    boolean updateRecipe(Long id, UpdateRecipeRequest updateRecipeRequest);

    /**
     * 根据id获取菜谱
     *
     * @param id
     * @return
     */
    RecipeVo getRecipeById(Long id);

    /**
     * 分页查询菜谱
     *
     * @param recipeQueryPageRequest
     * @return
     */
    Page<RecipeVo> listRecipePage(RecipeQueryPageRequest recipeQueryPageRequest);

    /**
     * 修改菜谱封面图片
     *
     * @param recipeId 菜谱id
     * @param image    图片文件
     * @return 图片URL
     */
    String updateRecipeImage(Long recipeId, org.springframework.web.multipart.MultipartFile image);
}

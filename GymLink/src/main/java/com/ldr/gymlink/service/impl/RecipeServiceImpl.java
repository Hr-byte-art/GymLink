package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.mapper.RecipeMapper;
import com.ldr.gymlink.model.dto.recipe.AddRecipeRequest;
import com.ldr.gymlink.model.dto.recipe.RecipeQueryPageRequest;
import com.ldr.gymlink.model.dto.recipe.UpdateRecipeRequest;
import com.ldr.gymlink.model.entity.Recipe;
import com.ldr.gymlink.model.vo.RecipeVo;
import com.ldr.gymlink.service.RecipeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 木子宸
 * @description 针对表【recipe(健身菜谱表)】的数据库操作Service实现
 * @createDate 2025-12-01 21:36:28
 */
@Service
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe>
        implements RecipeService {

    @Override
    public RecipeVo addRecipe(AddRecipeRequest addRecipeRequest) {
        Recipe recipe = new Recipe();
        BeanUtils.copyProperties(addRecipeRequest, recipe);
        recipe.setCreateTime(new Date());
        boolean save = this.save(recipe);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        RecipeVo recipeVo = new RecipeVo();
        BeanUtils.copyProperties(recipe, recipeVo);
        return recipeVo;
    }

    @Override
    public boolean deleteRecipe(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return this.removeById(id);
    }

    @Override
    public boolean updateRecipe(Long id, UpdateRecipeRequest updateRecipeRequest) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Recipe recipe = new Recipe();
        BeanUtils.copyProperties(updateRecipeRequest, recipe);
        recipe.setId(id);
        return this.updateById(recipe);
    }

    @Override
    public RecipeVo getRecipeById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Recipe recipe = this.getById(id);
        if (recipe == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        RecipeVo recipeVo = new RecipeVo();
        BeanUtils.copyProperties(recipe, recipeVo);
        return recipeVo;
    }

    @Override
    public Page<RecipeVo> listRecipePage(RecipeQueryPageRequest recipeQueryPageRequest) {
        long current = recipeQueryPageRequest.getPageNum();
        long pageSize = recipeQueryPageRequest.getPageSize();
        Page<Recipe> page = new Page<>(current, pageSize);
        QueryWrapper<Recipe> queryWrapper = new QueryWrapper<>();
        String title = recipeQueryPageRequest.getTitle();
        String tags = recipeQueryPageRequest.getTags();
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        if (StringUtils.isNotBlank(tags)) {
            queryWrapper.like("tags", tags);
        }
        Page<Recipe> recipePage = this.page(page, queryWrapper);
        Page<RecipeVo> recipeVoPage = new Page<>(current, pageSize, recipePage.getTotal());
        List<Recipe> records = recipePage.getRecords();
        List<RecipeVo> recipeVoList = new ArrayList<>();
        for (Recipe recipe : records) {
            RecipeVo recipeVo = new RecipeVo();
            BeanUtils.copyProperties(recipe, recipeVo);
            recipeVoList.add(recipeVo);
        }
        recipeVoPage.setRecords(recipeVoList);
        return recipeVoPage;
    }
}

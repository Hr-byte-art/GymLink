package com.ldr.gymlink.mapper;

import com.ldr.gymlink.model.entity.Recipe;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 木子宸
* @description 针对表【recipe(健身菜谱表)】的数据库操作Mapper
* @createDate 2025-12-01 21:36:28
* @Entity com.ldr.gymlink.model.entity.Recipe
*/
@Mapper
public interface RecipeMapper extends BaseMapper<Recipe> {

}





package com.ldr.gymlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ldr.gymlink.model.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏表 Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}

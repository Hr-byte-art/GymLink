package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.favorite.FavoriteQueryRequest;
import com.ldr.gymlink.model.dto.favorite.FavoriteRequest;
import com.ldr.gymlink.model.entity.Favorite;
import com.ldr.gymlink.model.vo.FavoriteVo;

/**
 * 收藏服务接口
 */
public interface FavoriteService extends IService<Favorite> {

    /**
     * 添加收藏
     *
     * @param studentId 学员ID
     * @param request   收藏请求
     * @return 是否成功
     */
    boolean addFavorite(Long studentId, FavoriteRequest request);

    /**
     * 取消收藏
     *
     * @param studentId 学员ID
     * @param request   取消收藏请求
     * @return 是否成功
     */
    boolean removeFavorite(Long studentId, FavoriteRequest request);

    /**
     * 切换收藏状态（已收藏则取消，未收藏则添加）
     *
     * @param studentId 学员ID
     * @param request   收藏请求
     * @return 当前是否已收藏
     */
    boolean toggleFavorite(Long studentId, FavoriteRequest request);

    /**
     * 检查是否已收藏
     *
     * @param studentId 学员ID
     * @param targetId  目标ID
     * @param type      收藏类型
     * @return 是否已收藏
     */
    boolean isFavorite(Long studentId, Long targetId, Integer type);

    /**
     * 获取收藏列表（分页）
     *
     * @param studentId 学员ID
     * @param request   查询请求
     * @return 收藏列表
     */
    Page<FavoriteVo> listFavorites(Long studentId, FavoriteQueryRequest request);

    /**
     * 获取某类型的收藏数量
     *
     * @param studentId 学员ID
     * @param type      收藏类型
     * @return 收藏数量
     */
    long countFavorites(Long studentId, Integer type);
}

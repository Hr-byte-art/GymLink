package com.ldr.gymlink.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.favorite.FavoriteQueryRequest;
import com.ldr.gymlink.model.dto.favorite.FavoriteRequest;
import com.ldr.gymlink.model.vo.FavoriteVo;
import com.ldr.gymlink.service.FavoriteService;
import com.ldr.gymlink.utils.ResultUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        boolean result = favoriteService.addFavorite(userId, request);
        return ResultUtils.success(result);
    }

    /**
     * 取消收藏
     */
    @PostMapping("/remove")
    public BaseResponse<Boolean> removeFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        boolean result = favoriteService.removeFavorite(userId, request);
        return ResultUtils.success(result);
    }

    /**
     * 切换收藏状态
     */
    @PostMapping("/toggle")
    public BaseResponse<Boolean> toggleFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        boolean isFavorite = favoriteService.toggleFavorite(userId, request);
        return ResultUtils.success(isFavorite);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public BaseResponse<Boolean> checkFavorite(@RequestParam Long targetId, @RequestParam Integer type) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            // 未登录时返回 false，不抛异常
            return ResultUtils.success(false);
        }
        boolean isFavorite = favoriteService.isFavorite(userId, targetId, type);
        return ResultUtils.success(isFavorite);
    }

    /**
     * 获取收藏列表（分页）
     */
    @GetMapping("/list")
    public BaseResponse<Page<FavoriteVo>> listFavorites(FavoriteQueryRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Page<FavoriteVo> page = favoriteService.listFavorites(userId, request);
        return ResultUtils.success(page);
    }

    /**
     * 获取收藏数量
     */
    @GetMapping("/count")
    public BaseResponse<Long> countFavorites(@RequestParam(required = false) Integer type) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long count = favoriteService.countFavorites(userId, type);
        return ResultUtils.success(count);
    }
}

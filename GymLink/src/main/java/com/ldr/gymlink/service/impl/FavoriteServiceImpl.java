package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.mapper.FavoriteMapper;
import com.ldr.gymlink.model.dto.favorite.FavoriteQueryRequest;
import com.ldr.gymlink.model.dto.favorite.FavoriteRequest;
import com.ldr.gymlink.model.entity.*;
import com.ldr.gymlink.model.enums.FavoriteTypeEnum;
import com.ldr.gymlink.model.vo.FavoriteVo;
import com.ldr.gymlink.service.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务实现
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Resource
    private EquipmentService equipmentService;

    @Resource
    private CoachService coachService;

    @Resource
    private CourseService courseService;

    @Resource
    private RecipeService recipeService;

    @Override
    public boolean addFavorite(Long studentId, FavoriteRequest request) {
        Long targetId = request.getTargetIdAsLong();
        
        // 校验收藏类型
        FavoriteTypeEnum typeEnum = FavoriteTypeEnum.getByValue(request.getType());
        if (typeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的收藏类型");
        }

        // 校验目标是否存在
        validateTargetExists(targetId, request.getType());

        // 检查是否已收藏
        if (isFavorite(studentId, targetId, request.getType())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "已经收藏过了");
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setStudentId(studentId);
        favorite.setTargetId(targetId);
        favorite.setType(request.getType());
        favorite.setCreateTime(new Date());

        return save(favorite);
    }

    @Override
    public boolean removeFavorite(Long studentId, FavoriteRequest request) {
        Long targetId = request.getTargetIdAsLong();
        
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getStudentId, studentId)
                .eq(Favorite::getTargetId, targetId)
                .eq(Favorite::getType, request.getType());

        return remove(wrapper);
    }

    @Override
    public boolean toggleFavorite(Long studentId, FavoriteRequest request) {
        Long targetId = request.getTargetIdAsLong();
        
        if (isFavorite(studentId, targetId, request.getType())) {
            removeFavorite(studentId, request);
            return false; // 取消收藏后返回 false
        } else {
            // 校验收藏类型
            FavoriteTypeEnum typeEnum = FavoriteTypeEnum.getByValue(request.getType());
            if (typeEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的收藏类型");
            }
            // 校验目标是否存在
            validateTargetExists(targetId, request.getType());

            Favorite favorite = new Favorite();
            favorite.setStudentId(studentId);
            favorite.setTargetId(targetId);
            favorite.setType(request.getType());
            favorite.setCreateTime(new Date());
            save(favorite);
            return true; // 添加收藏后返回 true
        }
    }

    @Override
    public boolean isFavorite(Long studentId, Long targetId, Integer type) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getStudentId, studentId)
                .eq(Favorite::getTargetId, targetId)
                .eq(Favorite::getType, type);

        return count(wrapper) > 0;
    }

    @Override
    public Page<FavoriteVo> listFavorites(Long studentId, FavoriteQueryRequest request) {
        Page<Favorite> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getStudentId, studentId);
        if (request.getType() != null) {
            wrapper.eq(Favorite::getType, request.getType());
        }
        wrapper.orderByDesc(Favorite::getCreateTime);

        Page<Favorite> favoritePage = page(page, wrapper);

        // 转换为 VO
        Page<FavoriteVo> voPage = new Page<>(favoritePage.getCurrent(), favoritePage.getSize(), favoritePage.getTotal());
        List<FavoriteVo> voList = favoritePage.getRecords().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public long countFavorites(Long studentId, Integer type) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getStudentId, studentId);
        if (type != null) {
            wrapper.eq(Favorite::getType, type);
        }
        return count(wrapper);
    }

    /**
     * 校验目标是否存在
     */
    private void validateTargetExists(Long targetId, Integer type) {
        FavoriteTypeEnum typeEnum = FavoriteTypeEnum.getByValue(type);
        if (typeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的收藏类型");
        }

        boolean exists = switch (typeEnum) {
            case EQUIPMENT -> equipmentService.getById(targetId) != null;
            case COACH -> coachService.getById(targetId) != null;
            case COURSE -> courseService.getById(targetId) != null;
            case RECIPE -> recipeService.getById(targetId) != null;
        };

        if (!exists) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "收藏目标不存在");
        }
    }

    /**
     * 转换为 VO
     */
    private FavoriteVo convertToVo(Favorite favorite) {
        FavoriteVo vo = new FavoriteVo();
        vo.setId(favorite.getId());
        vo.setTargetId(favorite.getTargetId());
        vo.setType(favorite.getType());
        vo.setCreateTime(favorite.getCreateTime());

        FavoriteTypeEnum typeEnum = FavoriteTypeEnum.getByValue(favorite.getType());
        if (typeEnum != null) {
            vo.setTypeName(typeEnum.getDesc());

            // 获取目标详情
            switch (typeEnum) {
                case EQUIPMENT -> {
                    Equipment equipment = equipmentService.getById(favorite.getTargetId());
                    if (equipment != null) {
                        vo.setTargetName(equipment.getName());
                        vo.setTargetImage(equipment.getImage());
                        vo.setTargetDescription(equipment.getDescription());
                    }
                }
                case COACH -> {
                    Coach coach = coachService.getById(favorite.getTargetId());
                    if (coach != null) {
                        vo.setTargetName(coach.getName());
                        vo.setTargetImage(coach.getAvatar());
                        vo.setTargetDescription(coach.getIntro());
                    }
                }
                case COURSE -> {
                    Course course = courseService.getById(favorite.getTargetId());
                    if (course != null) {
                        vo.setTargetName(course.getName());
                        vo.setTargetImage(course.getImage());
                        vo.setTargetDescription(course.getDescription());
                    }
                }
                case RECIPE -> {
                    Recipe recipe = recipeService.getById(favorite.getTargetId());
                    if (recipe != null) {
                        vo.setTargetName(recipe.getTitle());
                        vo.setTargetImage(recipe.getCoverImage());
                        vo.setTargetDescription(recipe.getContent());
                    }
                }
            }
        }

        return vo;
    }
}

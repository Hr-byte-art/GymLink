package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.mapper.CoachMapper;
import com.ldr.gymlink.model.dto.coach.AddCoachRequest;
import com.ldr.gymlink.model.dto.coach.CoachQueryPageRequest;
import com.ldr.gymlink.model.dto.coach.UpdateCoachRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.enums.UserRoleEnum;
import com.ldr.gymlink.model.vo.CoachVo;
import com.ldr.gymlink.service.CoachService;
import com.ldr.gymlink.service.UserService;
import com.ldr.gymlink.utils.ThrowUtils;
import jakarta.annotation.Resource;

import java.util.Date;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 木子宸
 * @description 针对表【coach】的数据库操作Service实现
 * @createDate 2025-11-29 14:40:18
 */
@Service
public class CoachServiceImpl extends ServiceImpl<CoachMapper, Coach>
        implements CoachService {

    @Resource
    private UserService userService;

    @Override
    public CoachVo getCoachByUserId(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户id不能为空");
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        String role = user.getRole();
        CoachVo coachVo = new CoachVo();
        if (!UserRoleEnum.COACH.getValue().equals(role)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户身份信息有误，请联系管理员查看");
        }
        Coach coach = this.getOne(new LambdaQueryWrapper<Coach>().eq(Coach::getId, user.getAssociatedUserId()));
        ThrowUtils.throwIf(coach == null, ErrorCode.NOT_FOUND_ERROR, "教练信息不存在");
        BeanUtils.copyProperties(coach, coachVo);
        return coachVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoachVo addCoach(AddCoachRequest addCoachRequest) {
        String password = addCoachRequest.getPassword();
        String string = userService.encryptPassword(password);
        addCoachRequest.setPassword(string);
        Coach coach = new Coach();
        BeanUtils.copyProperties(addCoachRequest, coach);
        coach.setCreateTime(new Date());
        boolean save = this.save(coach);

        User user = new User();
        user.setUsername(addCoachRequest.getUsername());
        user.setPassword(addCoachRequest.getPassword());
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setIsDelete(0);
        user.setRole(UserRoleEnum.COACH.getValue());
        user.setAssociatedUserId(coach.getId());

        boolean save1 = userService.save(user);
        if (!save || !save1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败，请稍后重试");
        }
        CoachVo coachVo = new CoachVo();
        BeanUtils.copyProperties(coach, coachVo);
        return coachVo;
    }

    @Override
    public Page<CoachVo> listCoachPage(CoachQueryPageRequest coachQueryPageRequest) {
        ThrowUtils.throwIf(coachQueryPageRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int pageSize = coachQueryPageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        int pageNum = coachQueryPageRequest.getPageNum();
        LambdaQueryWrapper<Coach> queryWrapper = userService.getCoachQueryWrapper(coachQueryPageRequest);
        Page<Coach> page = this.page(Page.of(pageNum, pageSize), queryWrapper);

        Page<CoachVo> coachVoPage = new Page<>(pageNum, pageSize);
        coachVoPage.setTotal(page.getTotal());
        coachVoPage.setRecords(page.getRecords().stream().map(coach -> {
            CoachVo coachVo = new CoachVo();
            BeanUtils.copyProperties(coach, coachVo);
            return coachVo;
        }).toList());
        return coachVoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCoach(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "教练id不能为空");
        Coach coach = this.getById(id);
        ThrowUtils.throwIf(coach == null, ErrorCode.NOT_FOUND_ERROR, "教练不存在");

        // 级联删除：删除关联的 User 记录
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getAssociatedUserId, id)
                .eq(User::getRole, UserRoleEnum.COACH.getValue());
        userService.remove(userQueryWrapper);

        // 删除教练记录
        return this.removeById(id);
    }

    @Override
    public boolean updateCoach(Long id, UpdateCoachRequest updateCoachRequest) {
        Coach coach = this.getById(id);
        if (coach == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        BeanUtils.copyProperties(updateCoachRequest, coach);
        return this.updateById(coach);
    }

    @Override
    public CoachVo getCoachById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "教练id不能为空");
        Coach coach = this.getById(id);
        ThrowUtils.throwIf(coach == null, ErrorCode.NOT_FOUND_ERROR, "教练不存在");
        CoachVo coachVo = new CoachVo();
        BeanUtils.copyProperties(coach, coachVo);
        return coachVo;
    }
}

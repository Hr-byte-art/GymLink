package com.ldr.gymlink.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.experience.AddExperienceRequest;
import com.ldr.gymlink.model.dto.experience.ExperienceQueryPageRequest;
import com.ldr.gymlink.model.dto.experience.GetUserReactionRequest;
import com.ldr.gymlink.model.dto.experience.UpdateExperienceRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.Experience;
import com.ldr.gymlink.model.entity.Student;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.vo.ExperienceVo;
import com.ldr.gymlink.service.CoachService;
import com.ldr.gymlink.service.CommentService;
import com.ldr.gymlink.service.ExperienceReactionService;
import com.ldr.gymlink.service.ExperienceService;
import com.ldr.gymlink.mapper.ExperienceMapper;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.service.UserService;
import com.ldr.gymlink.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author 木子宸
 * @description 针对表【experience(健身经验/社区帖子表)】的数据库操作Service实现
 * @createDate 2025-12-02 20:39:04
 */
@Service
public class ExperienceServiceImpl extends ServiceImpl<ExperienceMapper, Experience>
        implements ExperienceService {

    @Resource
    private UserService userService;

    @Resource
    private StudentService studentService;

    @Resource
    private CoachService coachService;

    @Resource
    private ExperienceReactionService experienceReactionService;

    @Resource
    private CommentService commentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExperienceVo addExperience(AddExperienceRequest addExperienceRequest) {
        ThrowUtils.throwIf(addExperienceRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        Experience experience = new Experience();
        BeanUtils.copyProperties(addExperienceRequest, experience);
        experience.setCreateTime(new Date());
        experience.setViewCount(0L); // 初始化浏览量为0
        boolean save = this.save(experience);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发布健身经验失败，请稍后重试");
        }

        ExperienceVo experienceVo = new ExperienceVo();
        BeanUtils.copyProperties(experience, experienceVo);
        return experienceVo;
    }

    @Override
    public Page<ExperienceVo> listExperiencePage(ExperienceQueryPageRequest experienceQueryPageRequest) {
        ThrowUtils.throwIf(experienceQueryPageRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int pageSize = experienceQueryPageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 条经验");
        int pageNum = experienceQueryPageRequest.getPageNum();

        LambdaQueryWrapper<Experience> queryWrapper = getExperienceQueryWrapper(experienceQueryPageRequest);
        Page<Experience> page = this.page(Page.of(pageNum, pageSize), queryWrapper);

        // 获取当前登录用户ID（如果已登录）
        Long currentUserId = null;
        try {
            if (StpUtil.isLogin()) {
                currentUserId = StpUtil.getLoginIdAsLong();
            }
        } catch (Exception ignored) {
        }
        final Long loginUserId = currentUserId;

        Page<ExperienceVo> experienceVoPage = new Page<>(pageNum, pageSize);
        experienceVoPage.setTotal(page.getTotal());
        experienceVoPage.setRecords(page.getRecords().stream().map(experience -> {
            ExperienceVo experienceVo = new ExperienceVo();
            BeanUtils.copyProperties(experience, experienceVo);
            // 填充用户信息
            fillUserInfo(experienceVo);
            // 填充点赞数
            experienceVo.setLikeCount(experienceReactionService.getLikesCount(experience.getId()));
            // 填充评论数
            experienceVo.setCommentCount(commentService.getCommentCount(experience.getId()));
            // 填充当前用户的点赞状态
            fillUserReaction(experienceVo, loginUserId);
            return experienceVo;
        }).toList());
        return experienceVoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteExperience(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "经验id不能为空");
        Experience experience = this.getById(id);
        ThrowUtils.throwIf(experience == null, ErrorCode.NOT_FOUND_ERROR, "健身经验不存在");
        // 删除经验记录
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateExperience(Long id, UpdateExperienceRequest updateExperienceRequest) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "经验id不能为空");
        Experience experience = this.getById(id);
        if (experience == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "健身经验不存在");
        }
        BeanUtils.copyProperties(updateExperienceRequest, experience);
        return this.updateById(experience);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExperienceVo getExperienceById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "经验id不能为空");
        Experience experience = this.getById(id);
        ThrowUtils.throwIf(experience == null, ErrorCode.NOT_FOUND_ERROR, "健身经验不存在");

        // 增加浏览量
        experience.setViewCount(experience.getViewCount() == null ? 1L : experience.getViewCount() + 1);
        this.updateById(experience);

        ExperienceVo experienceVo = new ExperienceVo();
        BeanUtils.copyProperties(experience, experienceVo);
        // 填充用户信息
        fillUserInfo(experienceVo);
        // 填充点赞数
        experienceVo.setLikeCount(experienceReactionService.getLikesCount(id));
        // 填充当前用户的点赞状态
        Long currentUserId = null;
        try {
            if (StpUtil.isLogin()) {
                currentUserId = StpUtil.getLoginIdAsLong();
            }
        } catch (Exception ignored) {
        }
        fillUserReaction(experienceVo, currentUserId);
        return experienceVo;
    }

    /**
     * 填充当前用户对帖子的反应状态
     */
    private void fillUserReaction(ExperienceVo experienceVo, Long loginUserId) {
        if (experienceVo == null || loginUserId == null) {
            experienceVo.setIsLiked(false);
            return;
        }
        try {
            GetUserReactionRequest request = new GetUserReactionRequest();
            request.setExperienceId(experienceVo.getId());
            request.setUserId(loginUserId);
            Integer reactionType = experienceReactionService.getUserReactionWithoutAuth(request);
            experienceVo.setUserReactionType(reactionType);
            experienceVo.setIsLiked(reactionType != null && reactionType == 1);
        } catch (Exception e) {
            experienceVo.setIsLiked(false);
        }
    }

    /**
     * 填充用户信息（名称和头像）
     * userId 存储的是 User 表的 id，通过 User.associatedUserId 关联到 Coach/Student 表
     *
     * @param experienceVo 经验VO
     */
    private void fillUserInfo(ExperienceVo experienceVo) {
        if (experienceVo == null || experienceVo.getUserId() == null) {
            return;
        }
        Long userId = experienceVo.getUserId();
        Integer userRole = experienceVo.getUserRole();

        try {
            // 从 User 表获取用户信息，包含 associatedUserId
            User user = userService.getById(userId);
            if (user != null && user.getAssociatedUserId() != null) {
                Long associatedUserId = user.getAssociatedUserId();
                // 根据角色获取详细信息（名称和头像）
                if (userRole != null && userRole == 1) {
                    // 教练：通过 associatedUserId 查询教练信息
                    Coach coach = coachService.getById(associatedUserId);
                    if (coach != null) {
                        experienceVo.setUserName(coach.getName());
                        experienceVo.setUserAvatar(coach.getAvatar());
                    } else {
                        experienceVo.setUserName(user.getUsername());
                    }
                } else {
                    // 学员：通过 associatedUserId 查询学员信息
                    Student student = studentService.getById(associatedUserId);
                    if (student != null) {
                        experienceVo.setUserName(student.getName());
                        experienceVo.setUserAvatar(student.getAvatar());
                    } else {
                        experienceVo.setUserName(user.getUsername());
                    }
                }
            } else if (user != null) {
                experienceVo.setUserName(user.getUsername());
            }
        } catch (Exception e) {
            // 获取用户信息失败，使用默认值
            experienceVo.setUserName("用户" + userId);
        }
    }

    /**
     * 获取健身经验查询条件
     *
     * @param experienceQueryPageRequest 查询条件
     * @return 查询条件
     */
    private LambdaQueryWrapper<Experience> getExperienceQueryWrapper(
            ExperienceQueryPageRequest experienceQueryPageRequest) {
        LambdaQueryWrapper<Experience> queryWrapper = new LambdaQueryWrapper<>();
        if (experienceQueryPageRequest == null) {
            return queryWrapper;
        }

        String title = experienceQueryPageRequest.getTitle();
        Long userId = experienceQueryPageRequest.getUserId();
        Integer userRole = experienceQueryPageRequest.getUserRole();

        // 模糊查询标题
        queryWrapper.like(StringUtils.isNotBlank(title), Experience::getTitle, title);
        // 精确查询用户ID
        queryWrapper.eq(userId != null, Experience::getUserId, userId);
        // 精确查询用户角色
        queryWrapper.eq(userRole != null, Experience::getUserRole, userRole);
        // 按创建时间降序排列
        queryWrapper.orderByDesc(Experience::getCreateTime);

        return queryWrapper;
    }
}

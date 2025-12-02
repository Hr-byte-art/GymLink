package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.experience.AddExperienceRequest;
import com.ldr.gymlink.model.dto.experience.ExperienceQueryPageRequest;
import com.ldr.gymlink.model.dto.experience.UpdateExperienceRequest;
import com.ldr.gymlink.model.entity.Experience;
import com.ldr.gymlink.model.vo.ExperienceVo;
import com.ldr.gymlink.service.ExperienceService;
import com.ldr.gymlink.mapper.ExperienceMapper;
import com.ldr.gymlink.utils.ThrowUtils;
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

        Page<ExperienceVo> experienceVoPage = new Page<>(pageNum, pageSize);
        experienceVoPage.setTotal(page.getTotal());
        experienceVoPage.setRecords(page.getRecords().stream().map(experience -> {
            ExperienceVo experienceVo = new ExperienceVo();
            BeanUtils.copyProperties(experience, experienceVo);
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
        return experienceVo;
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

package com.ldr.gymlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.announcement.AddAnnouncementRequest;
import com.ldr.gymlink.model.dto.announcement.AnnouncementQueryPageRequest;
import com.ldr.gymlink.model.dto.announcement.UpdateAnnouncementRequest;
import com.ldr.gymlink.model.entity.Announcement;
import com.ldr.gymlink.model.vo.AnnouncementVo;
import com.ldr.gymlink.model.vo.UnreadAnnouncementVo;
import com.ldr.gymlink.service.AnnouncementService;
import com.ldr.gymlink.mapper.AnnouncementMapper;
import com.ldr.gymlink.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author 木子宸
 * @description 针对表【announcement(系统公告表)】的数据库操作Service实现
 * @createDate 2025-12-02 20:39:04
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Redis key 前缀：用户已读公告
     */
    private static final String ANNOUNCEMENT_READ_KEY_PREFIX = "announcement:read:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementVo addAnnouncement(AddAnnouncementRequest addAnnouncementRequest) {
        ThrowUtils.throwIf(addAnnouncementRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(addAnnouncementRequest, announcement);
        announcement.setCreateTime(new Date());
        boolean save = this.save(announcement);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加公告失败，请稍后重试");
        }

        AnnouncementVo announcementVo = new AnnouncementVo();
        BeanUtils.copyProperties(announcement, announcementVo);
        return announcementVo;
    }

    @Override
    public Page<AnnouncementVo> listAnnouncementPage(AnnouncementQueryPageRequest announcementQueryPageRequest) {
        ThrowUtils.throwIf(announcementQueryPageRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int pageSize = announcementQueryPageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 条公告");
        int pageNum = announcementQueryPageRequest.getPageNum();

        LambdaQueryWrapper<Announcement> queryWrapper = getAnnouncementQueryWrapper(announcementQueryPageRequest);
        Page<Announcement> page = this.page(Page.of(pageNum, pageSize), queryWrapper);

        Page<AnnouncementVo> announcementVoPage = new Page<>(pageNum, pageSize);
        announcementVoPage.setTotal(page.getTotal());
        announcementVoPage.setRecords(page.getRecords().stream().map(announcement -> {
            AnnouncementVo announcementVo = new AnnouncementVo();
            BeanUtils.copyProperties(announcement, announcementVo);
            return announcementVo;
        }).toList());
        return announcementVoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAnnouncement(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "公告id不能为空");
        Announcement announcement = this.getById(id);
        ThrowUtils.throwIf(announcement == null, ErrorCode.NOT_FOUND_ERROR, "公告不存在");
        // 删除公告记录
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAnnouncement(Long id, UpdateAnnouncementRequest updateAnnouncementRequest) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "公告id不能为空");
        Announcement announcement = this.getById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "公告不存在");
        }
        BeanUtils.copyProperties(updateAnnouncementRequest, announcement);
        return this.updateById(announcement);
    }

    @Override
    public AnnouncementVo getAnnouncementById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "公告id不能为空");
        Announcement announcement = this.getById(id);
        ThrowUtils.throwIf(announcement == null, ErrorCode.NOT_FOUND_ERROR, "公告不存在");
        AnnouncementVo announcementVo = new AnnouncementVo();
        BeanUtils.copyProperties(announcement, announcementVo);
        return announcementVo;
    }

    /**
     * 获取公告查询条件
     *
     * @param announcementQueryPageRequest 查询条件
     * @return 查询条件
     */
    private LambdaQueryWrapper<Announcement> getAnnouncementQueryWrapper(
            AnnouncementQueryPageRequest announcementQueryPageRequest) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        if (announcementQueryPageRequest == null) {
            return queryWrapper;
        }

        String title = announcementQueryPageRequest.getTitle();
        Long adminId = announcementQueryPageRequest.getAdminId();
        Integer timeRange = announcementQueryPageRequest.getTimeRange();

        // 模糊查询公告标题
        queryWrapper.like(StringUtils.isNotBlank(title), Announcement::getTitle, title);
        // 精确查询管理员ID
        queryWrapper.eq(adminId != null, Announcement::getAdminId, adminId);
        // 时间范围查询
        if (timeRange != null && timeRange > 0) {
            Date startDate = new Date(System.currentTimeMillis() - (long) timeRange * 24 * 60 * 60 * 1000);
            queryWrapper.ge(Announcement::getCreateTime, startDate);
        }
        // 按创建时间降序排列
        queryWrapper.orderByDesc(Announcement::getCreateTime);

        return queryWrapper;
    }

    @Override
    public UnreadAnnouncementVo getUnreadAnnouncement(Long userId) {
        // 获取7天内的公告数量和最新公告
        Date sevenDaysAgo = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L);

        // 查询7天内所有公告（按时间倒序）
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Announcement::getCreateTime, sevenDaysAgo)
                .orderByDesc(Announcement::getCreateTime);
        java.util.List<Announcement> announcements = this.list(queryWrapper);

        // 没有7天内的公告，不推送
        if (announcements == null || announcements.isEmpty()) {
            return null;
        }

        // 检查用户已读的公告ID
        String redisKey = ANNOUNCEMENT_READ_KEY_PREFIX + userId;
        String readAnnouncementId = stringRedisTemplate.opsForValue().get(redisKey);

        // 计算未读公告数量
        int unreadCount = 0;
        Announcement latestUnread = null;
        for (Announcement announcement : announcements) {
            // 如果用户没有已读记录，或者公告ID大于已读ID，则为未读
            if (readAnnouncementId == null || announcement.getId() > Long.parseLong(readAnnouncementId)) {
                unreadCount++;
                if (latestUnread == null) {
                    latestUnread = announcement;
                }
            }
        }

        // 没有未读公告
        if (latestUnread == null) {
            return null;
        }

        // 构建返回结果
        UnreadAnnouncementVo result = new UnreadAnnouncementVo();
        AnnouncementVo announcementVo = new AnnouncementVo();
        BeanUtils.copyProperties(latestUnread, announcementVo);
        result.setAnnouncement(announcementVo);
        result.setUnreadCount(unreadCount);
        result.setMissedCount(unreadCount - 1); // 错过的数量 = 总未读 - 当前显示的1条
        return result;
    }

    @Override
    public void markAnnouncementAsRead(Long userId, Long announcementId) {
        String redisKey = ANNOUNCEMENT_READ_KEY_PREFIX + userId;
        // 记录用户已读的公告ID，设置7天过期
        stringRedisTemplate.opsForValue().set(redisKey, announcementId.toString(), 7, TimeUnit.DAYS);
    }
}

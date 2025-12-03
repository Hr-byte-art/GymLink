package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.model.entity.Announcement;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.dto.announcement.AddAnnouncementRequest;
import com.ldr.gymlink.model.dto.announcement.AnnouncementQueryPageRequest;
import com.ldr.gymlink.model.dto.announcement.UpdateAnnouncementRequest;
import com.ldr.gymlink.model.vo.AnnouncementVo;

/**
 * @author 木子宸
 * @description 针对表【announcement(系统公告表)】的数据库操作Service
 * @createDate 2025-12-02 20:39:04
 */
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 添加公告
     *
     * @param addAnnouncementRequest 添加公告请求
     * @return 公告信息
     */
    AnnouncementVo addAnnouncement(AddAnnouncementRequest addAnnouncementRequest);

    /**
     * 获取所有公告信息(分页)
     *
     * @param announcementQueryPageRequest 分页查询请求
     * @return 公告信息列表
     */
    Page<AnnouncementVo> listAnnouncementPage(AnnouncementQueryPageRequest announcementQueryPageRequest);

    /**
     * 删除公告
     *
     * @param id 公告id
     * @return 是否删除成功
     */
    boolean deleteAnnouncement(Long id);

    /**
     * 修改公告信息
     *
     * @param id                        公告id
     * @param updateAnnouncementRequest 修改公告信息请求
     * @return 是否修改成功
     */
    boolean updateAnnouncement(Long id, UpdateAnnouncementRequest updateAnnouncementRequest);

    /**
     * 根据id获取公告信息
     *
     * @param id 公告id
     * @return 公告信息
     */
    AnnouncementVo getAnnouncementById(Long id);
}

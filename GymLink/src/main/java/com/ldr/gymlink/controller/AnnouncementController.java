package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.announcement.AddAnnouncementRequest;
import com.ldr.gymlink.model.dto.announcement.AnnouncementQueryPageRequest;
import com.ldr.gymlink.model.dto.announcement.UpdateAnnouncementRequest;
import com.ldr.gymlink.model.vo.AnnouncementVo;
import com.ldr.gymlink.model.vo.UnreadAnnouncementVo;
import com.ldr.gymlink.model.vo.UserVo;
import com.ldr.gymlink.service.AnnouncementService;
import com.ldr.gymlink.service.UserService;
import com.ldr.gymlink.utils.ResultUtils;
import com.ldr.gymlink.utils.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 20:49:00
 * @Description 公告管理
 */
@RestController
@RequestMapping("/announcement")
@Tag(name = "公告管理")
public class AnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    @Resource
    private UserService userService;

    @PostMapping("/addAnnouncement")
    @Operation(summary = "添加公告")
    public BaseResponse<AnnouncementVo> addAnnouncement(@RequestBody AddAnnouncementRequest addAnnouncementRequest) {
        // 自动获取当前登录用户ID作为adminId
        UserVo loginUser = userService.getLoginUser();
        addAnnouncementRequest.setAdminId(loginUser.getId());
        AnnouncementVo announcementVo = announcementService.addAnnouncement(addAnnouncementRequest);
        return ResultUtils.success(announcementVo);
    }

    @PostMapping("/listAnnouncement")
    @Operation(summary = "分页查询公告信息")
    public BaseResponse<Page<AnnouncementVo>> listAnnouncement(
            @RequestBody AnnouncementQueryPageRequest announcementQueryPageRequest) {
        Page<AnnouncementVo> announcementVos = announcementService.listAnnouncementPage(announcementQueryPageRequest);
        return ResultUtils.success(announcementVos);
    }

    @GetMapping("/deleteAnnouncement")
    @Operation(summary = "删除公告信息")
    public BaseResponse<Boolean> deleteAnnouncement(@RequestParam Long id) {
        boolean delete = announcementService.deleteAnnouncement(id);
        return ResultUtils.success(delete);
    }

    @PostMapping("/updateAnnouncement")
    @Operation(summary = "更新公告信息")
    public BaseResponse<Boolean> updateAnnouncement(
            @RequestBody UpdateAnnouncementRequest updateAnnouncementRequest) {
        Long id = updateAnnouncementRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "公告id不能为空");
        boolean update = announcementService.updateAnnouncement(id, updateAnnouncementRequest);
        return ResultUtils.success(update);
    }

    @GetMapping("/getAnnouncementById")
    @Operation(summary = "获取公告信息")
    public BaseResponse<AnnouncementVo> getAnnouncementById(@RequestParam Long announcementId) {
        AnnouncementVo announcementVo = announcementService.getAnnouncementById(announcementId);
        return ResultUtils.success(announcementVo);
    }

    @GetMapping("/getUnreadAnnouncement")
    @Operation(summary = "获取用户未读公告")
    public BaseResponse<UnreadAnnouncementVo> getUnreadAnnouncement(@RequestParam Long userId) {
        UnreadAnnouncementVo result = announcementService.getUnreadAnnouncement(userId);
        return ResultUtils.success(result);
    }

    @PostMapping("/markAsRead")
    @Operation(summary = "标记公告为已读")
    public BaseResponse<Boolean> markAsRead(@RequestParam Long userId, @RequestParam Long announcementId) {
        announcementService.markAnnouncementAsRead(userId, announcementId);
        return ResultUtils.success(true);
    }
}

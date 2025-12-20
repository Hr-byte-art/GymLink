package com.ldr.gymlink.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.manager.CosManager;
import com.ldr.gymlink.mapper.CoachMapper;
import com.ldr.gymlink.model.dto.coach.AddCoachRequest;
import com.ldr.gymlink.model.dto.coach.CoachQueryPageRequest;
import com.ldr.gymlink.model.dto.coach.UpdateCoachRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.Student;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.enums.UserRoleEnum;
import com.ldr.gymlink.model.vo.CoachAppointmentVo;
import com.ldr.gymlink.model.vo.CoachVo;
import com.ldr.gymlink.model.vo.StudentVo;
import com.ldr.gymlink.service.CoachAppointmentService;
import com.ldr.gymlink.service.CoachService;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.service.UserService;
import com.ldr.gymlink.utils.ThrowUtils;
import com.ldr.gymlink.mq.message.AppointmentMessage;
import com.ldr.gymlink.mq.producer.AppointmentMessageProducer;
import com.luciad.imageio.webp.WebPWriteParam;
import jakarta.annotation.Resource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

/**
 * @author 木子宸
 * @description 针对表【coach】的数据库操作Service实现
 * @createDate 2025-11-29 14:40:18
 */
@Service
@Slf4j
public class CoachServiceImpl extends ServiceImpl<CoachMapper, Coach>
        implements CoachService {

    @Resource
    private UserService userService;

    @Resource
    private CoachAppointmentService coachAppointmentService;

    @Resource
    private StudentService studentService;

    @Resource
    private CosManager cosManager;

    @Resource
    private AppointmentMessageProducer appointmentMessageProducer;

    /**
     * 允许的图片格式
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");

    /**
     * 最大文件大小（5MB）
     */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

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
        // 查看用户名是否存在
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUsername, addCoachRequest.getUsername());
        User user1 = userService.getOne(userLambdaQueryWrapper);

        if (user1 != null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户名已存在,请更换");
        }
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

    @Override
    public boolean bookingCoach(com.ldr.gymlink.model.dto.coach.BookingCoachRequest bookingCoachRequest) {
        ThrowUtils.throwIf(bookingCoachRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long coachId = bookingCoachRequest.getCoachId();
        Long studentId = bookingCoachRequest.getStudentId();
        Date appointTime = bookingCoachRequest.getAppointTime();
        Date endTime = bookingCoachRequest.getEndTime();

        ThrowUtils.throwIf(coachId == null, ErrorCode.PARAMS_ERROR, "教练ID不能为空");
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        ThrowUtils.throwIf(appointTime == null, ErrorCode.PARAMS_ERROR, "开始时间不能为空");
        ThrowUtils.throwIf(endTime == null, ErrorCode.PARAMS_ERROR, "结束时间不能为空");
        ThrowUtils.throwIf(endTime.before(appointTime) || endTime.equals(appointTime), 
                ErrorCode.PARAMS_ERROR, "结束时间必须晚于开始时间");

        // 检查教练是否存在
        Coach coach = this.getById(coachId);
        ThrowUtils.throwIf(coach == null, ErrorCode.NOT_FOUND_ERROR, "教练不存在");

        // 检查教练在该时间段是否已有预约（时间冲突检测）
        // 冲突条件：新预约的开始时间 < 已有预约的结束时间 AND 新预约的结束时间 > 已有预约的开始时间
        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> coachConflictWrapper = new LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment>()
                .eq(com.ldr.gymlink.model.entity.CoachAppointment::getCoachId, coachId)
                .in(com.ldr.gymlink.model.entity.CoachAppointment::getStatus, 0, 1) // 待确认或已确认
                .and(w -> w
                        .lt(com.ldr.gymlink.model.entity.CoachAppointment::getAppointTime, endTime)
                        .gt(com.ldr.gymlink.model.entity.CoachAppointment::getEndTime, appointTime)
                );
        long coachConflictCount = coachAppointmentService.count(coachConflictWrapper);
        if (coachConflictCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该教练在此时间段已有其他预约");
        }

        // 检查学员在该时间段是否已有预约
        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> studentConflictWrapper =
                new LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment>()
                .eq(com.ldr.gymlink.model.entity.CoachAppointment::getStudentId, studentId)
                .in(com.ldr.gymlink.model.entity.CoachAppointment::getStatus, 0, 1)
                .and(w -> w
                        .lt(com.ldr.gymlink.model.entity.CoachAppointment::getAppointTime, endTime)
                        .gt(com.ldr.gymlink.model.entity.CoachAppointment::getEndTime, appointTime)
                );
        long studentConflictCount = coachAppointmentService.count(studentConflictWrapper);
        if (studentConflictCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您在此时间段已有其他预约");
        }

        // 检查当天预约教练的次数(限制为3次)
        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> dailyLimitWrapper = new LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment>()
                .eq(com.ldr.gymlink.model.entity.CoachAppointment::getStudentId, studentId)
                .in(com.ldr.gymlink.model.entity.CoachAppointment::getStatus, 0, 1)
                .apply("DATE(create_time) = DATE(NOW())");
        long dailyCount = coachAppointmentService.count(dailyLimitWrapper);
        if (dailyCount >= 3) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当天预约教练次数已达上限（最多3次）");
        }

        com.ldr.gymlink.model.entity.CoachAppointment coachAppointment = new com.ldr.gymlink.model.entity.CoachAppointment();
        coachAppointment.setStudentId(studentId);
        coachAppointment.setCoachId(coachId);
        coachAppointment.setAppointTime(appointTime);
        coachAppointment.setEndTime(endTime);
        coachAppointment.setMessage(bookingCoachRequest.getMessage());
        coachAppointment.setStatus(0); // 待确认
        coachAppointment.setCreateTime(new Date());
        boolean saved = coachAppointmentService.save(coachAppointment);

        // 发送MQ消息通知教练
        if (saved) {
            try {
                Student student = studentService.getById(studentId);
                // 通过 User 表获取教练和学员的 userId
                Long coachUserId = getUserIdByAssociatedId(coachId, UserRoleEnum.COACH.getValue());
                Long studentUserId = getUserIdByAssociatedId(studentId, UserRoleEnum.STUDENT.getValue());
                AppointmentMessage message = AppointmentMessage.builder()
                        .type(AppointmentMessage.TYPE_NEW_APPOINTMENT)
                        .appointmentId(coachAppointment.getId())
                        .receiverUserId(coachUserId)
                        .senderUserId(studentUserId)
                        .senderName(student != null ? student.getName() : "学员")
                        .appointTime(appointTime)
                        .endTime(endTime)
                        .message(bookingCoachRequest.getMessage())
                        .build();
                appointmentMessageProducer.sendAppointmentNotification(message);
            } catch (Exception e) {
                log.error("发送预约通知失败", e);
            }
        }
        return saved;
    }

    @Override
    public boolean cancelCoachAppointment(Long appointmentId) {
        ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "预约ID不能为空");
        com.ldr.gymlink.model.entity.CoachAppointment appointment = coachAppointmentService.getById(appointmentId);
        ThrowUtils.throwIf(appointment == null, ErrorCode.NOT_FOUND_ERROR, "预约记录不存在");

        // 只有待确认或已确认的预约可以取消
        if (appointment.getStatus() != 0 && appointment.getStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前预约状态不允许取消");
        }

        // 直接删除预约记录（与器材预约取消保持一致）
        return coachAppointmentService.removeById(appointmentId);
    }

    @Override
    public boolean confirmCoachAppointment(Long appointmentId) {
        ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "预约ID不能为空");
        com.ldr.gymlink.model.entity.CoachAppointment appointment = coachAppointmentService.getById(appointmentId);
        ThrowUtils.throwIf(appointment == null, ErrorCode.NOT_FOUND_ERROR, "预约记录不存在");

        // 只有待确认的预约可以确认
        if (appointment.getStatus() != 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前预约状态不允许确认");
        }

        appointment.setStatus(1); // 已确认
        boolean updated = coachAppointmentService.updateById(appointment);

        // 发送MQ消息通知学员
        if (updated) {
            try {
                Coach coach = this.getById(appointment.getCoachId());
                // 通过 User 表获取教练和学员的 userId
                Long coachUserId = getUserIdByAssociatedId(appointment.getCoachId(), UserRoleEnum.COACH.getValue());
                Long studentUserId = getUserIdByAssociatedId(appointment.getStudentId(), UserRoleEnum.STUDENT.getValue());
                AppointmentMessage message = AppointmentMessage.builder()
                        .type(AppointmentMessage.TYPE_APPOINTMENT_CONFIRMED)
                        .appointmentId(appointmentId)
                        .receiverUserId(studentUserId)
                        .senderUserId(coachUserId)
                        .senderName(coach != null ? coach.getName() : "教练")
                        .appointTime(appointment.getAppointTime())
                        .endTime(appointment.getEndTime())
                        .build();
                appointmentMessageProducer.sendAppointmentNotification(message);
            } catch (Exception e) {
                log.error("发送预约确认通知失败", e);
            }
        }
        return updated;
    }

    @Override
    public boolean rejectCoachAppointment(Long appointmentId) {
        ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "预约ID不能为空");
        com.ldr.gymlink.model.entity.CoachAppointment appointment = coachAppointmentService.getById(appointmentId);
        ThrowUtils.throwIf(appointment == null, ErrorCode.NOT_FOUND_ERROR, "预约记录不存在");

        // 只有待确认的预约可以拒绝
        if (appointment.getStatus() != 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前预约状态不允许拒绝");
        }

        appointment.setStatus(2); // 已拒绝
        boolean updated = coachAppointmentService.updateById(appointment);

        // 发送MQ消息通知学员
        if (updated) {
            try {
                Coach coach = this.getById(appointment.getCoachId());
                // 通过 User 表获取教练和学员的 userId
                Long coachUserId = getUserIdByAssociatedId(appointment.getCoachId(), UserRoleEnum.COACH.getValue());
                Long studentUserId = getUserIdByAssociatedId(appointment.getStudentId(), UserRoleEnum.STUDENT.getValue());
                AppointmentMessage message = AppointmentMessage.builder()
                        .type(AppointmentMessage.TYPE_APPOINTMENT_REJECTED)
                        .appointmentId(appointmentId)
                        .receiverUserId(studentUserId)
                        .senderUserId(coachUserId)
                        .senderName(coach != null ? coach.getName() : "教练")
                        .appointTime(appointment.getAppointTime())
                        .endTime(appointment.getEndTime())
                        .build();
                appointmentMessageProducer.sendAppointmentNotification(message);
            } catch (Exception e) {
                log.error("发送预约拒绝通知失败", e);
            }
        }
        return updated;
    }

    @Override
    public Page<CoachAppointmentVo> listStudentCoachAppointmentPage(
            com.ldr.gymlink.model.dto.coach.StudentCoachAppointmentQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long studentId = request.getStudentId();
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");

        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(com.ldr.gymlink.model.entity.CoachAppointment::getStudentId, studentId);
        if (request.getStatus() != null) {
            queryWrapper.eq(com.ldr.gymlink.model.entity.CoachAppointment::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(com.ldr.gymlink.model.entity.CoachAppointment::getCreateTime);

        Page<com.ldr.gymlink.model.entity.CoachAppointment> page = coachAppointmentService
                .page(Page.of(pageNum, pageSize), queryWrapper);
        return getCoachAppointmentVoPage(page);
    }

    @Override
    public Page<CoachAppointmentVo> listAllCoachAppointmentPage(
            com.ldr.gymlink.model.dto.coach.AllCoachAppointmentQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> queryWrapper = new LambdaQueryWrapper<>();
        if (request.getStudentId() != null) {
            queryWrapper.eq(com.ldr.gymlink.model.entity.CoachAppointment::getStudentId, request.getStudentId());
        }
        if (request.getCoachId() != null) {
            queryWrapper.eq(com.ldr.gymlink.model.entity.CoachAppointment::getCoachId, request.getCoachId());
        }
        if (request.getStatus() != null) {
            queryWrapper.eq(com.ldr.gymlink.model.entity.CoachAppointment::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(com.ldr.gymlink.model.entity.CoachAppointment::getCreateTime);

        Page<com.ldr.gymlink.model.entity.CoachAppointment> page = coachAppointmentService
                .page(Page.of(pageNum, pageSize), queryWrapper);
        return getCoachAppointmentVoPage(page);
    }

    @Override
    public Page<CoachAppointmentVo> listAppointmentsByCoach(
            com.ldr.gymlink.model.dto.coach.CoachAppointmentSearchRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long coachId = request.getCoachId();
        ThrowUtils.throwIf(coachId == null, ErrorCode.PARAMS_ERROR, "教练ID不能为空");

        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(com.ldr.gymlink.model.entity.CoachAppointment::getCoachId, coachId);
        if (request.getStatus() != null) {
            queryWrapper.eq(com.ldr.gymlink.model.entity.CoachAppointment::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(com.ldr.gymlink.model.entity.CoachAppointment::getCreateTime);

        Page<com.ldr.gymlink.model.entity.CoachAppointment> page = coachAppointmentService
                .page(Page.of(pageNum, pageSize), queryWrapper);
        return getCoachAppointmentVoPage(page);
    }

    @Override
    public Page<CoachAppointmentVo> listAppointmentsByTimeRange(
            com.ldr.gymlink.model.dto.coach.CoachTimeAppointmentSearchRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        ThrowUtils.throwIf(startTime == null || endTime == null, ErrorCode.PARAMS_ERROR, "时间段不能为空");

        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(com.ldr.gymlink.model.entity.CoachAppointment::getAppointTime, startTime, endTime);
        if (request.getStatus() != null) {
            queryWrapper.eq(com.ldr.gymlink.model.entity.CoachAppointment::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(com.ldr.gymlink.model.entity.CoachAppointment::getCreateTime);

        Page<com.ldr.gymlink.model.entity.CoachAppointment> page = coachAppointmentService
                .page(Page.of(pageNum, pageSize), queryWrapper);
        return getCoachAppointmentVoPage(page);
    }

    @Override
    public Page<CoachAppointmentVo> listStudentAppointmentsByTimeRange(
            com.ldr.gymlink.model.dto.coach.StudentCoachTimeAppointmentSearchRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long studentId = request.getStudentId();
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        ThrowUtils.throwIf(startTime == null || endTime == null, ErrorCode.PARAMS_ERROR, "时间段不能为空");

        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(com.ldr.gymlink.model.entity.CoachAppointment::getStudentId, studentId);
        queryWrapper.between(com.ldr.gymlink.model.entity.CoachAppointment::getAppointTime, startTime, endTime);
        if (request.getStatus() != null) {
            queryWrapper.eq(com.ldr.gymlink.model.entity.CoachAppointment::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(com.ldr.gymlink.model.entity.CoachAppointment::getCreateTime);

        Page<com.ldr.gymlink.model.entity.CoachAppointment> page = coachAppointmentService
                .page(Page.of(pageNum, pageSize), queryWrapper);
        return getCoachAppointmentVoPage(page);
    }

    @Override
    public String updateCoachAvatar(Long coachId, MultipartFile file) {
        validateImageFile(file);
        File compressedImage = null;
        try {
            String key = "avatar/coach/" + StrUtil.format("{}/{}.{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")), coachId , "webp");

            compressedImage = compressImage(file);

            String result = cosManager.uploadFile(key, compressedImage);

            // 9. 更新数据库中的头像URL
            Coach coach = this.getById(coachId);
            if (coach == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            }
            coach.setAvatar(result);
            boolean update = this.updateById(coach);
            if (!update) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新用户头像失败");
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // 10. 清理临时文件，确保即使上传失败也能删除
            compressedImage.delete();
        }
    }


    /**
     * 转换教练预约为VO对象并填充教练和学员姓名
     */
    private Page<CoachAppointmentVo> getCoachAppointmentVoPage(
            Page<com.ldr.gymlink.model.entity.CoachAppointment> page) {
        Page<CoachAppointmentVo> voPage = new Page<>(page.getCurrent(), page.getSize(),
                page.getTotal());
        java.util.List<CoachAppointmentVo> voList = page.getRecords().stream()
                .map(appointment -> {
                    CoachAppointmentVo vo = new CoachAppointmentVo();
                    BeanUtils.copyProperties(appointment, vo);
                    // 填充教练姓名
                    Coach coach = this.getById(appointment.getCoachId());
                    if (coach != null) {
                        vo.setCoachName(coach.getName());
                    }
                    // 填充学员姓名
                    StudentVo student = studentService
                            .getStudentById(appointment.getStudentId());
                    if (student != null) {
                        vo.setStudentName(student.getName());
                    }
                    return vo;
                }).collect(java.util.stream.Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 校验图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过5MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持 JPG、PNG、WEBP、GIF 格式的图片");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 压缩图片
     */
    private File compressImage(MultipartFile multipartFile) throws IOException {
        float quality = 0.5f;
        File oldFile = null;
        File newFile = null;

        try {
            // 1. 创建临时文件并写入上传的内容
            oldFile = File.createTempFile("old-", getFileExtension(multipartFile.getOriginalFilename()));
            multipartFile.transferTo(oldFile);

            // 2. 创建压缩后的文件（WebP 格式）
            newFile = File.createTempFile("compressed-", ".webp");

            // 3. 转换为 WebP 格式并压缩
            convertImage2Webp(oldFile, newFile, quality);

            return newFile;

        } catch (Exception e) {
            // 清理失败的临时文件
            if (newFile != null && newFile.exists()) {
                newFile.delete();
            }
            throw new IOException("图片压缩失败", e);
        } finally {
            // 4. 清理原始临时文件
            if (oldFile != null && oldFile.exists()) {
                oldFile.delete();
            }
        }
    }

    /**
     * 将图片转换为 WebP 格式
     */
    private void convertImage2Webp(File oldFile, File newFile, float quality) throws IOException {
        ImageWriter writer = null;
        FileImageOutputStream output = null;

        try {
            // 1. 读取原始图片
            BufferedImage image = ImageIO.read(oldFile);
            if (image == null) {
                throw new IOException("无法读取图片文件，可能格式不支持");
            }

            // 2. 创建 WebP ImageWriter 实例
            writer = ImageIO.getImageWritersByMIMEType("image/webp").next();

            // 3. 配置编码参数
            WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
            writeParam.setCompressionMode(WebPWriteParam.MODE_EXPLICIT);
            // "Lossy"-有损,"Lossless"-无损
            writeParam.setCompressionType(writeParam.getCompressionTypes()[0]);
            writeParam.setCompressionQuality(quality);

            // 4. 配置 ImageWriter 输出
            output = new FileImageOutputStream(newFile);
            writer.setOutput(output);

            // 5. 进行编码，生成 WebP 图片
            writer.write(null, new IIOImage(image, null, null), writeParam);

            log.info("图片压缩成功，原始大小: {} KB, 压缩后大小: {} KB",
                    oldFile.length() / 1024, newFile.length() / 1024);

        } catch (IOException e) {
            log.error("图片转换为 WebP 失败", e);
            throw e;  // 抛出异常，让调用方知道失败了
        } finally {
            // 6. 释放资源
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    log.error("关闭输出流失败", e);
                }
            }
            if (writer != null) {
                writer.dispose();
            }
        }
    }

    @Override
    public com.ldr.gymlink.model.vo.CoachStatisticsVo getCoachStatistics() {
        com.ldr.gymlink.model.vo.CoachStatisticsVo vo = new com.ldr.gymlink.model.vo.CoachStatisticsVo();

        // 1. 教练总数
        List<Coach> allCoaches = this.list();
        vo.setTotalCoach((long) allCoaches.size());

        // 2. 性别统计
        long maleCount = allCoaches.stream().filter(c -> c.getGender() != null && c.getGender() == 1).count();
        long femaleCount = allCoaches.stream().filter(c -> c.getGender() != null && c.getGender() == 2).count();
        vo.setMaleCount(maleCount);
        vo.setFemaleCount(femaleCount);

        // 3. 获取时间范围
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();

        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        Date weekStart = calendar.getTime();

        calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        Date monthStart = calendar.getTime();

        // 4. 预约统计
        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.ge(com.ldr.gymlink.model.entity.CoachAppointment::getCreateTime, todayStart);
        vo.setTodayAppointmentCount(coachAppointmentService.count(todayQuery));

        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> weekQuery = new LambdaQueryWrapper<>();
        weekQuery.ge(com.ldr.gymlink.model.entity.CoachAppointment::getCreateTime, weekStart);
        vo.setWeekAppointmentCount(coachAppointmentService.count(weekQuery));

        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> monthQuery = new LambdaQueryWrapper<>();
        monthQuery.ge(com.ldr.gymlink.model.entity.CoachAppointment::getCreateTime, monthStart);
        vo.setMonthAppointmentCount(coachAppointmentService.count(monthQuery));

        // 5. 预约状态统计
        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> pendingQuery = new LambdaQueryWrapper<>();
        pendingQuery.eq(com.ldr.gymlink.model.entity.CoachAppointment::getStatus, 0);
        vo.setPendingAppointmentCount(coachAppointmentService.count(pendingQuery));

        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> confirmedQuery = new LambdaQueryWrapper<>();
        confirmedQuery.eq(com.ldr.gymlink.model.entity.CoachAppointment::getStatus, 1);
        vo.setConfirmedAppointmentCount(coachAppointmentService.count(confirmedQuery));

        // 6. 专长分布统计
        java.util.Map<String, Long> specialtyMap = allCoaches.stream()
                .filter(c -> c.getSpecialty() != null && !c.getSpecialty().isEmpty())
                .collect(java.util.stream.Collectors.groupingBy(Coach::getSpecialty, java.util.stream.Collectors.counting()));
        List<com.ldr.gymlink.model.vo.CoachStatisticsVo.SpecialtyCountVo> specialtyStats = specialtyMap.entrySet().stream()
                .map(entry -> {
                    com.ldr.gymlink.model.vo.CoachStatisticsVo.SpecialtyCountVo sv = new com.ldr.gymlink.model.vo.CoachStatisticsVo.SpecialtyCountVo();
                    sv.setSpecialty(entry.getKey());
                    sv.setCount(entry.getValue());
                    return sv;
                }).collect(java.util.stream.Collectors.toList());
        vo.setSpecialtyStatistics(specialtyStats);

        // 7. 近7天预约趋势
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        List<com.ldr.gymlink.model.vo.CoachStatisticsVo.DailyCountVo> dailyTrend = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_MONTH, -i);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            Date dayStart = cal.getTime();
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            Date dayEnd = cal.getTime();

            LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> dayQuery = new LambdaQueryWrapper<>();
            dayQuery.ge(com.ldr.gymlink.model.entity.CoachAppointment::getCreateTime, dayStart)
                    .lt(com.ldr.gymlink.model.entity.CoachAppointment::getCreateTime, dayEnd);

            com.ldr.gymlink.model.vo.CoachStatisticsVo.DailyCountVo dcv = new com.ldr.gymlink.model.vo.CoachStatisticsVo.DailyCountVo();
            dcv.setDate(sdf.format(dayStart));
            dcv.setCount(coachAppointmentService.count(dayQuery));
            dailyTrend.add(dcv);
        }
        vo.setDailyAppointmentTrend(dailyTrend);

        // 8. 热门教练TOP10
        List<com.ldr.gymlink.model.entity.CoachAppointment> allAppointments = coachAppointmentService.list();
        java.util.Map<Long, Long> coachAppointmentMap = allAppointments.stream()
                .filter(a -> a.getCoachId() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        com.ldr.gymlink.model.entity.CoachAppointment::getCoachId,
                        java.util.stream.Collectors.counting()));
        java.util.Map<Long, Coach> coachMap = allCoaches.stream()
                .collect(java.util.stream.Collectors.toMap(Coach::getId, c -> c, (a, b) -> a));
        List<com.ldr.gymlink.model.vo.CoachStatisticsVo.CoachRankVo> hotCoaches = coachAppointmentMap.entrySet().stream()
                .map(entry -> {
                    com.ldr.gymlink.model.vo.CoachStatisticsVo.CoachRankVo crv = new com.ldr.gymlink.model.vo.CoachStatisticsVo.CoachRankVo();
                    crv.setCoachId(entry.getKey());
                    Coach coach = coachMap.get(entry.getKey());
                    crv.setCoachName(coach != null ? coach.getName() : "未知教练");
                    crv.setSpecialty(coach != null ? coach.getSpecialty() : "");
                    crv.setAppointmentCount(entry.getValue());
                    return crv;
                })
                .sorted((a, b) -> Long.compare(b.getAppointmentCount(), a.getAppointmentCount()))
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
        vo.setHotCoachRank(hotCoaches);

        // 9. 年龄分布
        List<com.ldr.gymlink.model.vo.CoachStatisticsVo.AgeDistributionVo> ageDistribution = new java.util.ArrayList<>();
        long age20_25 = allCoaches.stream().filter(c -> c.getAge() != null && c.getAge() >= 20 && c.getAge() < 25).count();
        long age25_30 = allCoaches.stream().filter(c -> c.getAge() != null && c.getAge() >= 25 && c.getAge() < 30).count();
        long age30_35 = allCoaches.stream().filter(c -> c.getAge() != null && c.getAge() >= 30 && c.getAge() < 35).count();
        long age35_40 = allCoaches.stream().filter(c -> c.getAge() != null && c.getAge() >= 35 && c.getAge() < 40).count();
        long age40Plus = allCoaches.stream().filter(c -> c.getAge() != null && c.getAge() >= 40).count();

        if (age20_25 > 0) { com.ldr.gymlink.model.vo.CoachStatisticsVo.AgeDistributionVo av = new com.ldr.gymlink.model.vo.CoachStatisticsVo.AgeDistributionVo(); av.setAgeRange("20-25岁"); av.setCount(age20_25); ageDistribution.add(av); }
        if (age25_30 > 0) { com.ldr.gymlink.model.vo.CoachStatisticsVo.AgeDistributionVo av = new com.ldr.gymlink.model.vo.CoachStatisticsVo.AgeDistributionVo(); av.setAgeRange("25-30岁"); av.setCount(age25_30); ageDistribution.add(av); }
        if (age30_35 > 0) { com.ldr.gymlink.model.vo.CoachStatisticsVo.AgeDistributionVo av = new com.ldr.gymlink.model.vo.CoachStatisticsVo.AgeDistributionVo(); av.setAgeRange("30-35岁"); av.setCount(age30_35); ageDistribution.add(av); }
        if (age35_40 > 0) { com.ldr.gymlink.model.vo.CoachStatisticsVo.AgeDistributionVo av = new com.ldr.gymlink.model.vo.CoachStatisticsVo.AgeDistributionVo(); av.setAgeRange("35-40岁"); av.setCount(age35_40); ageDistribution.add(av); }
        if (age40Plus > 0) { com.ldr.gymlink.model.vo.CoachStatisticsVo.AgeDistributionVo av = new com.ldr.gymlink.model.vo.CoachStatisticsVo.AgeDistributionVo(); av.setAgeRange("40岁以上"); av.setCount(age40Plus); ageDistribution.add(av); }
        vo.setAgeDistribution(ageDistribution);

        return vo;
    }

    /**
     * 通过关联ID和角色获取用户ID
     * @param associatedId 关联的教练ID或学员ID
     * @param role 角色（coach 或 student）
     * @return 用户ID
     */
    private Long getUserIdByAssociatedId(Long associatedId, String role) {
        if (associatedId == null) {
            return null;
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAssociatedUserId, associatedId)
                .eq(User::getRole, role);
        User user = userService.getOne(queryWrapper);
        return user != null ? user.getId() : null;
    }
}
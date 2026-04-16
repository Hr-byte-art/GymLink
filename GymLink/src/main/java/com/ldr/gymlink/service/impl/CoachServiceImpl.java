package com.ldr.gymlink.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.manager.CosManager;
import com.ldr.gymlink.mapper.CoachMapper;
import com.ldr.gymlink.model.dto.coach.AddCoachRequest;
import com.ldr.gymlink.model.dto.coach.AllCoachAppointmentQueryRequest;
import com.ldr.gymlink.model.dto.coach.BookingCoachRequest;
import com.ldr.gymlink.model.dto.coach.CoachAppointmentSearchRequest;
import com.ldr.gymlink.model.dto.coach.CoachQueryPageRequest;
import com.ldr.gymlink.model.dto.coach.CoachTimeAppointmentSearchRequest;
import com.ldr.gymlink.model.dto.coach.StudentCoachAppointmentQueryRequest;
import com.ldr.gymlink.model.dto.coach.StudentCoachTimeAppointmentSearchRequest;
import com.ldr.gymlink.model.dto.coach.UpdateCoachRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.CoachAppointment;
import com.ldr.gymlink.model.entity.Course;
import com.ldr.gymlink.model.entity.CourseOrder;
import com.ldr.gymlink.model.entity.Student;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.enums.UserRoleEnum;
import com.ldr.gymlink.model.vo.CoachAppointmentVo;
import com.ldr.gymlink.model.vo.CoachStatisticsVo;
import com.ldr.gymlink.model.vo.CoachVo;
import com.ldr.gymlink.model.vo.StudentVo;
import com.ldr.gymlink.mq.message.AppointmentMessage;
import com.ldr.gymlink.mq.producer.AppointmentMessageProducer;
import com.ldr.gymlink.service.CoachAppointmentService;
import com.ldr.gymlink.service.CoachService;
import com.ldr.gymlink.service.CourseOrderService;
import com.ldr.gymlink.service.CourseService;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.service.UserService;
import com.ldr.gymlink.utils.ThrowUtils;
import com.luciad.imageio.webp.WebPWriteParam;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CoachServiceImpl extends ServiceImpl<CoachMapper, Coach> implements CoachService {

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Resource
    private UserService userService;

    @Resource
    private CoachAppointmentService coachAppointmentService;

    @Resource
    private StudentService studentService;

    @Resource
    private CourseOrderService courseOrderService;

    @Resource
    private CourseService courseService;

    @Resource
    private CosManager cosManager;

    @Resource
    private AppointmentMessageProducer appointmentMessageProducer;

    @Override
    public CoachVo getCoachByUserId(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(!UserRoleEnum.COACH.getValue().equals(user.getRole()),
                ErrorCode.OPERATION_ERROR, "当前用户不是教练");

        Coach coach = this.getById(user.getAssociatedUserId());
        ThrowUtils.throwIf(coach == null, ErrorCode.NOT_FOUND_ERROR, "教练信息不存在");

        CoachVo coachVo = new CoachVo();
        BeanUtils.copyProperties(coach, coachVo);
        return coachVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoachVo addCoach(AddCoachRequest addCoachRequest) {
        String encryptedPassword = userService.encryptPassword(addCoachRequest.getPassword());
        addCoachRequest.setPassword(encryptedPassword);

        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, addCoachRequest.getUsername());
        ThrowUtils.throwIf(userService.getOne(userQueryWrapper) != null,
                ErrorCode.OPERATION_ERROR, "用户名已存在，请更换");

        Coach coach = new Coach();
        BeanUtils.copyProperties(addCoachRequest, coach);
        coach.setCreateTime(new Date());
        boolean saveCoach = this.save(coach);

        User user = new User();
        user.setUsername(addCoachRequest.getUsername());
        user.setPassword(addCoachRequest.getPassword());
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setIsDelete(0);
        user.setRole(UserRoleEnum.COACH.getValue());
        user.setAssociatedUserId(coach.getId());
        boolean saveUser = userService.save(user);

        ThrowUtils.throwIf(!saveCoach || !saveUser, ErrorCode.SYSTEM_ERROR, "新增教练失败");

        CoachVo coachVo = new CoachVo();
        BeanUtils.copyProperties(coach, coachVo);
        return coachVo;
    }

    @Override
    public Page<CoachVo> listCoachPage(CoachQueryPageRequest coachQueryPageRequest) {
        ThrowUtils.throwIf(coachQueryPageRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        int pageSize = coachQueryPageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 条数据");

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
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "教练ID不能为空");
        Coach coach = this.getById(id);
        ThrowUtils.throwIf(coach == null, ErrorCode.NOT_FOUND_ERROR, "教练不存在");

        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getAssociatedUserId, id)
                .eq(User::getRole, UserRoleEnum.COACH.getValue());
        userService.remove(userQueryWrapper);
        return this.removeById(id);
    }

    @Override
    public boolean updateCoach(Long id, UpdateCoachRequest updateCoachRequest) {
        Coach coach = this.getById(id);
        ThrowUtils.throwIf(coach == null, ErrorCode.NOT_FOUND_ERROR, "教练不存在");
        BeanUtils.copyProperties(updateCoachRequest, coach);
        return this.updateById(coach);
    }

    @Override
    public CoachVo getCoachById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "教练ID不能为空");
        Coach coach = this.getById(id);
        ThrowUtils.throwIf(coach == null, ErrorCode.NOT_FOUND_ERROR, "教练不存在");

        CoachVo coachVo = new CoachVo();
        BeanUtils.copyProperties(coach, coachVo);
        return coachVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bookingCoach(BookingCoachRequest bookingCoachRequest) {
        ThrowUtils.throwIf(bookingCoachRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        Long coachId = bookingCoachRequest.getCoachId();
        Long studentId = bookingCoachRequest.getStudentId();
        Date appointTime = bookingCoachRequest.getAppointTime();
        Date endTime = bookingCoachRequest.getEndTime();

        ThrowUtils.throwIf(coachId == null, ErrorCode.PARAMS_ERROR, "教练ID不能为空");
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        ThrowUtils.throwIf(appointTime == null, ErrorCode.PARAMS_ERROR, "开始时间不能为空");
        ThrowUtils.throwIf(endTime == null, ErrorCode.PARAMS_ERROR, "结束时间不能为空");
        ThrowUtils.throwIf(!endTime.after(appointTime), ErrorCode.PARAMS_ERROR, "结束时间必须晚于开始时间");

        Coach coach = this.getById(coachId);
        ThrowUtils.throwIf(coach == null, ErrorCode.NOT_FOUND_ERROR, "教练不存在");

        CourseOrder availableOrder = getAvailablePrivateCourseOrder(studentId, coachId);
        ThrowUtils.throwIf(availableOrder == null, ErrorCode.OPERATION_ERROR, "请先购买该教练的私教课程后再预约");

        LambdaQueryWrapper<CoachAppointment> coachConflictWrapper = new LambdaQueryWrapper<CoachAppointment>()
                .eq(CoachAppointment::getCoachId, coachId)
                .in(CoachAppointment::getStatus, 0, 1)
                .and(wrapper -> wrapper.lt(CoachAppointment::getAppointTime, endTime)
                        .gt(CoachAppointment::getEndTime, appointTime));
        ThrowUtils.throwIf(coachAppointmentService.count(coachConflictWrapper) > 0,
                ErrorCode.OPERATION_ERROR, "该教练在此时间段已有其他预约");

        LambdaQueryWrapper<CoachAppointment> studentConflictWrapper = new LambdaQueryWrapper<CoachAppointment>()
                .eq(CoachAppointment::getStudentId, studentId)
                .in(CoachAppointment::getStatus, 0, 1)
                .and(wrapper -> wrapper.lt(CoachAppointment::getAppointTime, endTime)
                        .gt(CoachAppointment::getEndTime, appointTime));
        ThrowUtils.throwIf(coachAppointmentService.count(studentConflictWrapper) > 0,
                ErrorCode.OPERATION_ERROR, "您在此时间段已有其他预约");

        LambdaQueryWrapper<CoachAppointment> dailyLimitWrapper = new LambdaQueryWrapper<CoachAppointment>()
                .eq(CoachAppointment::getStudentId, studentId)
                .in(CoachAppointment::getStatus, 0, 1)
                .apply("DATE(create_time) = DATE(NOW())");
        ThrowUtils.throwIf(coachAppointmentService.count(dailyLimitWrapper) >= 3,
                ErrorCode.OPERATION_ERROR, "当天预约教练次数已达上限");

        CoachAppointment coachAppointment = new CoachAppointment();
        coachAppointment.setStudentId(studentId);
        coachAppointment.setCoachId(coachId);
        coachAppointment.setOrderId(availableOrder.getId());
        coachAppointment.setCourseId(availableOrder.getCourseId());
        coachAppointment.setAppointTime(appointTime);
        coachAppointment.setEndTime(endTime);
        coachAppointment.setMessage(bookingCoachRequest.getMessage());
        coachAppointment.setStatus(0);
        coachAppointment.setCreateTime(new Date());

        decrementCourseOrderSession(availableOrder);
        boolean saved = coachAppointmentService.save(coachAppointment);

        if (saved) {
            try {
                Student student = studentService.getById(studentId);
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
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelCoachAppointment(Long appointmentId) {
        ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "预约ID不能为空");
        CoachAppointment appointment = coachAppointmentService.getById(appointmentId);
        ThrowUtils.throwIf(appointment == null, ErrorCode.NOT_FOUND_ERROR, "预约记录不存在");
        ThrowUtils.throwIf(appointment.getStatus() != 0 && appointment.getStatus() != 1,
                ErrorCode.OPERATION_ERROR, "当前预约状态不允许取消");

        restoreCourseOrderSession(appointment.getOrderId());
        return coachAppointmentService.removeById(appointmentId);
    }

    @Override
    public boolean confirmCoachAppointment(Long appointmentId) {
        ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "预约ID不能为空");
        CoachAppointment appointment = coachAppointmentService.getById(appointmentId);
        ThrowUtils.throwIf(appointment == null, ErrorCode.NOT_FOUND_ERROR, "预约记录不存在");
        ThrowUtils.throwIf(appointment.getStatus() != 0, ErrorCode.OPERATION_ERROR, "当前预约状态不允许确认");

        appointment.setStatus(1);
        boolean updated = coachAppointmentService.updateById(appointment);
        if (updated) {
            try {
                Coach coach = this.getById(appointment.getCoachId());
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
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectCoachAppointment(Long appointmentId) {
        ThrowUtils.throwIf(appointmentId == null, ErrorCode.PARAMS_ERROR, "预约ID不能为空");
        CoachAppointment appointment = coachAppointmentService.getById(appointmentId);
        ThrowUtils.throwIf(appointment == null, ErrorCode.NOT_FOUND_ERROR, "预约记录不存在");
        ThrowUtils.throwIf(appointment.getStatus() != 0, ErrorCode.OPERATION_ERROR, "当前预约状态不允许拒绝");

        appointment.setStatus(2);
        restoreCourseOrderSession(appointment.getOrderId());
        boolean updated = coachAppointmentService.updateById(appointment);
        if (updated) {
            try {
                Coach coach = this.getById(appointment.getCoachId());
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
    public Page<CoachAppointmentVo> listStudentCoachAppointmentPage(StudentCoachAppointmentQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        Long studentId = request.getStudentId();
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");

        LambdaQueryWrapper<CoachAppointment> queryWrapper = new LambdaQueryWrapper<CoachAppointment>()
                .eq(CoachAppointment::getStudentId, studentId);
        if (request.getStatus() != null) {
            queryWrapper.eq(CoachAppointment::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(CoachAppointment::getCreateTime);

        Page<CoachAppointment> page = coachAppointmentService.page(
                Page.of(request.getPageNum(), request.getPageSize()), queryWrapper);
        return getCoachAppointmentVoPage(page);
    }

    @Override
    public Page<CoachAppointmentVo> listAllCoachAppointmentPage(AllCoachAppointmentQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        LambdaQueryWrapper<CoachAppointment> queryWrapper = new LambdaQueryWrapper<>();
        if (request.getStudentId() != null) {
            queryWrapper.eq(CoachAppointment::getStudentId, request.getStudentId());
        }
        if (request.getCoachId() != null) {
            queryWrapper.eq(CoachAppointment::getCoachId, request.getCoachId());
        }
        if (request.getStatus() != null) {
            queryWrapper.eq(CoachAppointment::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(CoachAppointment::getCreateTime);

        Page<CoachAppointment> page = coachAppointmentService.page(
                Page.of(request.getPageNum(), request.getPageSize()), queryWrapper);
        return getCoachAppointmentVoPage(page);
    }

    @Override
    public Page<CoachAppointmentVo> listAppointmentsByCoach(CoachAppointmentSearchRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        Long coachId = request.getCoachId();
        ThrowUtils.throwIf(coachId == null, ErrorCode.PARAMS_ERROR, "教练ID不能为空");

        LambdaQueryWrapper<CoachAppointment> queryWrapper = new LambdaQueryWrapper<CoachAppointment>()
                .eq(CoachAppointment::getCoachId, coachId);
        if (request.getStatus() != null) {
            queryWrapper.eq(CoachAppointment::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(CoachAppointment::getCreateTime);

        Page<CoachAppointment> page = coachAppointmentService.page(
                Page.of(request.getPageNum(), request.getPageSize()), queryWrapper);
        return getCoachAppointmentVoPage(page);
    }

    @Override
    public Page<CoachAppointmentVo> listAppointmentsByTimeRange(CoachTimeAppointmentSearchRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        ThrowUtils.throwIf(startTime == null || endTime == null, ErrorCode.PARAMS_ERROR, "时间段不能为空");

        LambdaQueryWrapper<CoachAppointment> queryWrapper = new LambdaQueryWrapper<CoachAppointment>()
                .between(CoachAppointment::getAppointTime, startTime, endTime);
        if (request.getStatus() != null) {
            queryWrapper.eq(CoachAppointment::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(CoachAppointment::getCreateTime);

        Page<CoachAppointment> page = coachAppointmentService.page(
                Page.of(request.getPageNum(), request.getPageSize()), queryWrapper);
        return getCoachAppointmentVoPage(page);
    }

    @Override
    public Page<CoachAppointmentVo> listStudentAppointmentsByTimeRange(StudentCoachTimeAppointmentSearchRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        Long studentId = request.getStudentId();
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        ThrowUtils.throwIf(startTime == null || endTime == null, ErrorCode.PARAMS_ERROR, "时间段不能为空");

        LambdaQueryWrapper<CoachAppointment> queryWrapper = new LambdaQueryWrapper<CoachAppointment>()
                .eq(CoachAppointment::getStudentId, studentId)
                .between(CoachAppointment::getAppointTime, startTime, endTime);
        if (request.getStatus() != null) {
            queryWrapper.eq(CoachAppointment::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(CoachAppointment::getCreateTime);

        Page<CoachAppointment> page = coachAppointmentService.page(
                Page.of(request.getPageNum(), request.getPageSize()), queryWrapper);
        return getCoachAppointmentVoPage(page);
    }

    @Override
    public String updateCoachAvatar(Long coachId, MultipartFile file) {
        validateImageFile(file);
        File compressedImage = null;
        try {
            String key = "avatar/coach/" + StrUtil.format("{}/{}.{}",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                    coachId,
                    "webp");
            compressedImage = compressImage(file);
            String result = cosManager.uploadFile(key, compressedImage);

            Coach coach = this.getById(coachId);
            ThrowUtils.throwIf(coach == null, ErrorCode.NOT_FOUND_ERROR, "教练不存在");
            coach.setAvatar(result);
            ThrowUtils.throwIf(!this.updateById(coach), ErrorCode.SYSTEM_ERROR, "更新教练头像失败");
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (compressedImage != null && compressedImage.exists()) {
                compressedImage.delete();
            }
        }
    }

    @Override
    public CoachStatisticsVo getCoachStatistics() {
        CoachStatisticsVo vo = new CoachStatisticsVo();
        List<Coach> allCoaches = this.list();
        vo.setTotalCoach((long) allCoaches.size());

        long maleCount = allCoaches.stream().filter(c -> c.getGender() != null && c.getGender() == 1).count();
        long femaleCount = allCoaches.stream().filter(c -> c.getGender() != null && c.getGender() == 2).count();
        vo.setMaleCount(maleCount);
        vo.setFemaleCount(femaleCount);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStart = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date monthStart = calendar.getTime();

        LambdaQueryWrapper<CoachAppointment> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.ge(CoachAppointment::getCreateTime, todayStart);
        vo.setTodayAppointmentCount(coachAppointmentService.count(todayQuery));

        LambdaQueryWrapper<CoachAppointment> weekQuery = new LambdaQueryWrapper<>();
        weekQuery.ge(CoachAppointment::getCreateTime, weekStart);
        vo.setWeekAppointmentCount(coachAppointmentService.count(weekQuery));

        LambdaQueryWrapper<CoachAppointment> monthQuery = new LambdaQueryWrapper<>();
        monthQuery.ge(CoachAppointment::getCreateTime, monthStart);
        vo.setMonthAppointmentCount(coachAppointmentService.count(monthQuery));

        LambdaQueryWrapper<CoachAppointment> pendingQuery = new LambdaQueryWrapper<>();
        pendingQuery.eq(CoachAppointment::getStatus, 0);
        vo.setPendingAppointmentCount(coachAppointmentService.count(pendingQuery));

        LambdaQueryWrapper<CoachAppointment> confirmedQuery = new LambdaQueryWrapper<>();
        confirmedQuery.eq(CoachAppointment::getStatus, 1);
        vo.setConfirmedAppointmentCount(coachAppointmentService.count(confirmedQuery));

        Map<String, Long> specialtyMap = allCoaches.stream()
                .filter(c -> StrUtil.isNotBlank(c.getSpecialty()))
                .collect(Collectors.groupingBy(Coach::getSpecialty, Collectors.counting()));
        List<CoachStatisticsVo.SpecialtyCountVo> specialtyStats = specialtyMap.entrySet().stream()
                .map(entry -> {
                    CoachStatisticsVo.SpecialtyCountVo item = new CoachStatisticsVo.SpecialtyCountVo();
                    item.setSpecialty(entry.getKey());
                    item.setCount(entry.getValue());
                    return item;
                })
                .toList();
        vo.setSpecialtyStatistics(specialtyStats);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<CoachStatisticsVo.DailyCountVo> dailyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Calendar dayCalendar = Calendar.getInstance();
            dayCalendar.add(Calendar.DAY_OF_MONTH, -i);
            dayCalendar.set(Calendar.HOUR_OF_DAY, 0);
            dayCalendar.set(Calendar.MINUTE, 0);
            dayCalendar.set(Calendar.SECOND, 0);
            dayCalendar.set(Calendar.MILLISECOND, 0);
            Date dayStart = dayCalendar.getTime();
            dayCalendar.add(Calendar.DAY_OF_MONTH, 1);
            Date dayEnd = dayCalendar.getTime();

            LambdaQueryWrapper<CoachAppointment> dayQuery = new LambdaQueryWrapper<>();
            dayQuery.ge(CoachAppointment::getCreateTime, dayStart)
                    .lt(CoachAppointment::getCreateTime, dayEnd);

            CoachStatisticsVo.DailyCountVo item = new CoachStatisticsVo.DailyCountVo();
            item.setDate(sdf.format(dayStart));
            item.setCount(coachAppointmentService.count(dayQuery));
            dailyTrend.add(item);
        }
        vo.setDailyAppointmentTrend(dailyTrend);

        List<CoachAppointment> allAppointments = coachAppointmentService.list();
        Map<Long, Long> coachAppointmentMap = allAppointments.stream()
                .filter(item -> item.getCoachId() != null)
                .collect(Collectors.groupingBy(CoachAppointment::getCoachId, Collectors.counting()));
        Map<Long, Coach> coachMap = allCoaches.stream()
                .collect(Collectors.toMap(Coach::getId, item -> item, (left, right) -> left));
        List<CoachStatisticsVo.CoachRankVo> hotCoaches = coachAppointmentMap.entrySet().stream()
                .map(entry -> {
                    CoachStatisticsVo.CoachRankVo item = new CoachStatisticsVo.CoachRankVo();
                    item.setCoachId(entry.getKey());
                    Coach coach = coachMap.get(entry.getKey());
                    item.setCoachName(coach != null ? coach.getName() : "未知教练");
                    item.setSpecialty(coach != null ? coach.getSpecialty() : "");
                    item.setAppointmentCount(entry.getValue());
                    return item;
                })
                .sorted((a, b) -> Long.compare(b.getAppointmentCount(), a.getAppointmentCount()))
                .limit(10)
                .toList();
        vo.setHotCoachRank(hotCoaches);

        List<CoachStatisticsVo.AgeDistributionVo> ageDistribution = new ArrayList<>();
        long age20To25 = allCoaches.stream().filter(c -> c.getAge() != null && c.getAge() >= 20 && c.getAge() < 25).count();
        long age25To30 = allCoaches.stream().filter(c -> c.getAge() != null && c.getAge() >= 25 && c.getAge() < 30).count();
        long age30To35 = allCoaches.stream().filter(c -> c.getAge() != null && c.getAge() >= 30 && c.getAge() < 35).count();
        long age35To40 = allCoaches.stream().filter(c -> c.getAge() != null && c.getAge() >= 35 && c.getAge() < 40).count();
        long age40Plus = allCoaches.stream().filter(c -> c.getAge() != null && c.getAge() >= 40).count();

        addAgeDistribution(ageDistribution, "20-25岁", age20To25);
        addAgeDistribution(ageDistribution, "25-30岁", age25To30);
        addAgeDistribution(ageDistribution, "30-35岁", age30To35);
        addAgeDistribution(ageDistribution, "35-40岁", age35To40);
        addAgeDistribution(ageDistribution, "40岁以上", age40Plus);
        vo.setAgeDistribution(ageDistribution);

        return vo;
    }

    private Page<CoachAppointmentVo> getCoachAppointmentVoPage(Page<CoachAppointment> page) {
        Page<CoachAppointmentVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<CoachAppointmentVo> voList = page.getRecords().stream().map(appointment -> {
            CoachAppointmentVo vo = new CoachAppointmentVo();
            BeanUtils.copyProperties(appointment, vo);

            Coach coach = this.getById(appointment.getCoachId());
            if (coach != null) {
                vo.setCoachName(coach.getName());
            }

            StudentVo student = studentService.getStudentById(appointment.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getName());
            }

            if (appointment.getCourseId() != null) {
                Course course = courseService.getById(appointment.getCourseId());
                if (course != null) {
                    vo.setCourseName(course.getName());
                }
            }
            return vo;
        }).toList();
        voPage.setRecords(voList);
        return voPage;
    }

    private CourseOrder getAvailablePrivateCourseOrder(Long studentId, Long coachId) {
        LambdaQueryWrapper<CourseOrder> queryWrapper = new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getStudentId, studentId)
                .eq(CourseOrder::getCoachId, coachId)
                .eq(CourseOrder::getDeliveryMode, 1)
                .eq(CourseOrder::getStatus, 1)
                .gt(CourseOrder::getRemainingSessions, 0)
                .orderByAsc(CourseOrder::getCreateTime)
                .last("limit 1");
        return courseOrderService.getOne(queryWrapper, false);
    }

    private void decrementCourseOrderSession(CourseOrder courseOrder) {
        ThrowUtils.throwIf(courseOrder == null, ErrorCode.NOT_FOUND_ERROR, "可用课程订单不存在");
        Integer remainingSessions = courseOrder.getRemainingSessions();
        ThrowUtils.throwIf(remainingSessions == null || remainingSessions <= 0,
                ErrorCode.OPERATION_ERROR, "课程剩余课次不足");
        courseOrder.setRemainingSessions(remainingSessions - 1);
        ThrowUtils.throwIf(!courseOrderService.updateById(courseOrder), ErrorCode.SYSTEM_ERROR, "扣减课程课次失败");
    }

    private void restoreCourseOrderSession(Long orderId) {
        if (orderId == null) {
            return;
        }
        CourseOrder courseOrder = courseOrderService.getById(orderId);
        if (courseOrder == null) {
            return;
        }
        int remainingSessions = courseOrder.getRemainingSessions() == null ? 0 : courseOrder.getRemainingSessions();
        int totalSessions = courseOrder.getTotalSessions() == null ? remainingSessions + 1 : courseOrder.getTotalSessions();
        courseOrder.setRemainingSessions(Math.min(remainingSessions + 1, totalSessions));
        courseOrderService.updateById(courseOrder);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅支持 JPG、PNG、WEBP、GIF 格式的图片");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    private File compressImage(MultipartFile multipartFile) throws IOException {
        float quality = 0.5f;
        File oldFile = null;
        File newFile = null;
        try {
            oldFile = File.createTempFile("old-", getFileExtension(multipartFile.getOriginalFilename()));
            multipartFile.transferTo(oldFile);

            newFile = File.createTempFile("compressed-", ".webp");
            convertImage2Webp(oldFile, newFile, quality);
            return newFile;
        } catch (Exception e) {
            if (newFile != null && newFile.exists()) {
                newFile.delete();
            }
            throw new IOException("图片压缩失败", e);
        } finally {
            if (oldFile != null && oldFile.exists()) {
                oldFile.delete();
            }
        }
    }

    private void convertImage2Webp(File oldFile, File newFile, float quality) throws IOException {
        ImageWriter writer = null;
        FileImageOutputStream output = null;
        try {
            BufferedImage image = ImageIO.read(oldFile);
            if (image == null) {
                throw new IOException("无法读取图片文件");
            }

            writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
            WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
            writeParam.setCompressionMode(WebPWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionType(writeParam.getCompressionTypes()[0]);
            writeParam.setCompressionQuality(quality);

            output = new FileImageOutputStream(newFile);
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), writeParam);

            log.info("图片压缩成功，原始大小 {} KB，压缩后大小 {} KB",
                    oldFile.length() / 1024, newFile.length() / 1024);
        } catch (IOException e) {
            log.error("图片转换为 WebP 失败", e);
            throw e;
        } finally {
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

    private void addAgeDistribution(List<CoachStatisticsVo.AgeDistributionVo> ageDistribution,
                                    String label,
                                    long count) {
        if (count <= 0) {
            return;
        }
        CoachStatisticsVo.AgeDistributionVo item = new CoachStatisticsVo.AgeDistributionVo();
        item.setAgeRange(label);
        item.setCount(count);
        ageDistribution.add(item);
    }

    private Long getUserIdByAssociatedId(Long associatedId, String role) {
        if (associatedId == null) {
            return null;
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getAssociatedUserId, associatedId)
                .eq(User::getRole, role);
        User user = userService.getOne(queryWrapper);
        return user != null ? user.getId() : null;
    }
}

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

    @Resource
    private com.ldr.gymlink.service.CoachAppointmentService coachAppointmentService;

    @Resource
    private com.ldr.gymlink.service.StudentService studentService;

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

    @Override
    public boolean bookingCoach(com.ldr.gymlink.model.dto.coach.BookingCoachRequest bookingCoachRequest) {
        ThrowUtils.throwIf(bookingCoachRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long coachId = bookingCoachRequest.getCoachId();
        Long studentId = bookingCoachRequest.getStudentId();
        Date appointTime = bookingCoachRequest.getAppointTime();

        ThrowUtils.throwIf(coachId == null, ErrorCode.PARAMS_ERROR, "教练ID不能为空");
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        ThrowUtils.throwIf(appointTime == null, ErrorCode.PARAMS_ERROR, "预约时间不能为空");

        // 检查教练是否存在
        Coach coach = this.getById(coachId);
        ThrowUtils.throwIf(coach == null, ErrorCode.NOT_FOUND_ERROR, "教练不存在");

        // 检查学员在同一时间是否已有预约
        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> wrapper = new LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment>()
                .eq(com.ldr.gymlink.model.entity.CoachAppointment::getStudentId, studentId)
                .eq(com.ldr.gymlink.model.entity.CoachAppointment::getAppointTime, appointTime)
                .in(com.ldr.gymlink.model.entity.CoachAppointment::getStatus, 0, 1); // 待确认或已确认
        long count = coachAppointmentService.count(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该时间段已有预约");
        }

        // 检查当天预约教练的次数(限制为3次)
        LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment> wrapper2 = new LambdaQueryWrapper<com.ldr.gymlink.model.entity.CoachAppointment>()
                .eq(com.ldr.gymlink.model.entity.CoachAppointment::getStudentId, studentId)
                .in(com.ldr.gymlink.model.entity.CoachAppointment::getStatus, 0, 1)
                .apply("DATE(create_time) = DATE(NOW())");
        long count2 = coachAppointmentService.count(wrapper2);
        if (count2 >= 3) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当天预约教练次数已达上限");
        }

        com.ldr.gymlink.model.entity.CoachAppointment coachAppointment = new com.ldr.gymlink.model.entity.CoachAppointment();
        coachAppointment.setStudentId(studentId);
        coachAppointment.setCoachId(coachId);
        coachAppointment.setAppointTime(appointTime);
        coachAppointment.setMessage(bookingCoachRequest.getMessage());
        coachAppointment.setStatus(0); // 待确认
        coachAppointment.setCreateTime(new Date());
        return coachAppointmentService.save(coachAppointment);
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

        appointment.setStatus(3); // 已取消
        return coachAppointmentService.updateById(appointment);
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
        return coachAppointmentService.updateById(appointment);
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
        return coachAppointmentService.updateById(appointment);
    }

    @Override
    public Page<com.ldr.gymlink.model.vo.CoachAppointmentVo> listStudentCoachAppointmentPage(
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
    public Page<com.ldr.gymlink.model.vo.CoachAppointmentVo> listAllCoachAppointmentPage(
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
    public Page<com.ldr.gymlink.model.vo.CoachAppointmentVo> listAppointmentsByCoach(
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
    public Page<com.ldr.gymlink.model.vo.CoachAppointmentVo> listAppointmentsByTimeRange(
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
    public Page<com.ldr.gymlink.model.vo.CoachAppointmentVo> listStudentAppointmentsByTimeRange(
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

    /**
     * 转换教练预约为VO对象并填充教练和学员姓名
     */
    private Page<com.ldr.gymlink.model.vo.CoachAppointmentVo> getCoachAppointmentVoPage(
            Page<com.ldr.gymlink.model.entity.CoachAppointment> page) {
        Page<com.ldr.gymlink.model.vo.CoachAppointmentVo> voPage = new Page<>(page.getCurrent(), page.getSize(),
                page.getTotal());
        java.util.List<com.ldr.gymlink.model.vo.CoachAppointmentVo> voList = page.getRecords().stream()
                .map(appointment -> {
                    com.ldr.gymlink.model.vo.CoachAppointmentVo vo = new com.ldr.gymlink.model.vo.CoachAppointmentVo();
                    BeanUtils.copyProperties(appointment, vo);
                    // 填充教练姓名
                    Coach coach = this.getById(appointment.getCoachId());
                    if (coach != null) {
                        vo.setCoachName(coach.getName());
                    }
                    // 填充学员姓名
                    com.ldr.gymlink.model.vo.StudentVo student = studentService
                            .getStudentById(appointment.getStudentId());
                    if (student != null) {
                        vo.setStudentName(student.getName());
                    }
                    return vo;
                }).collect(java.util.stream.Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}

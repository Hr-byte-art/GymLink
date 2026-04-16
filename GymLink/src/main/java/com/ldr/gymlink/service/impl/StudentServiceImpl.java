package com.ldr.gymlink.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.manager.CosManager;
import com.ldr.gymlink.mapper.CoachMapper;
import com.ldr.gymlink.mapper.StudentMapper;
import com.ldr.gymlink.model.dto.student.AddStudentRequest;
import com.ldr.gymlink.model.dto.student.PurchasedCourseQueryRequest;
import com.ldr.gymlink.model.dto.student.StudentQueryPageRequest;
import com.ldr.gymlink.model.dto.student.UpdateStudentRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.Course;
import com.ldr.gymlink.model.entity.CourseOrder;
import com.ldr.gymlink.model.entity.RechargeRecord;
import com.ldr.gymlink.model.entity.Student;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.enums.UserRoleEnum;
import com.ldr.gymlink.model.vo.PurchasedCourseVo;
import com.ldr.gymlink.model.vo.StudentVo;
import com.ldr.gymlink.service.CourseOrderService;
import com.ldr.gymlink.service.CourseService;
import com.ldr.gymlink.service.RechargeRecordService;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");

    @Resource
    private UserService userService;

    @Resource
    private RechargeRecordService rechargeRecordService;

    @Resource
    private CourseOrderService courseOrderService;

    @Resource
    private CourseService courseService;

    @Resource
    private CosManager cosManager;

    @Resource
    private CoachMapper coachMapper;

    @Resource
    private com.ldr.gymlink.mq.producer.RefundMessageProducer refundMessageProducer;

    @Resource
    private com.ldr.gymlink.mq.producer.CourseOrderMessageProducer courseOrderMessageProducer;

    @Override
    public StudentVo getStudentByUserId(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        StudentVo studentVo = new StudentVo();
        if (UserRoleEnum.STUDENT.getValue().equals(user.getRole())) {
            Student student = this.getById(user.getAssociatedUserId());
            ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员信息不存在");
            BeanUtils.copyProperties(student, studentVo);
        }
        return studentVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentVo addStudent(AddStudentRequest addStudentRequest) {
        String encryptedPassword = userService.encryptPassword(addStudentRequest.getPassword());
        addStudentRequest.setPassword(encryptedPassword);

        Student student = new Student();
        BeanUtils.copyProperties(addStudentRequest, student);
        student.setCreateTime(new Date());
        boolean saveStudent = this.save(student);

        User user = new User();
        user.setUsername(addStudentRequest.getUsername());
        user.setPassword(addStudentRequest.getPassword());
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setIsDelete(0);
        user.setRole(UserRoleEnum.STUDENT.getValue());
        user.setAssociatedUserId(student.getId());
        boolean saveUser = userService.save(user);

        ThrowUtils.throwIf(!saveStudent || !saveUser, ErrorCode.SYSTEM_ERROR, "新增学员失败");

        StudentVo studentVo = new StudentVo();
        BeanUtils.copyProperties(student, studentVo);
        return studentVo;
    }

    @Override
    public Page<StudentVo> listStudentPage(StudentQueryPageRequest studentQueryPageRequest) {
        ThrowUtils.throwIf(studentQueryPageRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        int pageSize = studentQueryPageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 条数据");

        int pageNum = studentQueryPageRequest.getPageNum();
        LambdaQueryWrapper<Student> queryWrapper = userService.getQueryWrapper(studentQueryPageRequest);
        Page<Student> page = this.page(Page.of(pageNum, pageSize), queryWrapper);

        Page<StudentVo> studentVoPage = new Page<>(pageNum, pageSize);
        studentVoPage.setTotal(page.getTotal());
        studentVoPage.setRecords(page.getRecords().stream().map(student -> {
            StudentVo studentVo = new StudentVo();
            BeanUtils.copyProperties(student, studentVo);
            return studentVo;
        }).toList());
        return studentVoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteStudent(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        Student student = this.getById(id);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");

        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getAssociatedUserId, id)
                .eq(User::getRole, UserRoleEnum.STUDENT.getValue());
        userService.remove(userQueryWrapper);
        return this.removeById(id);
    }

    @Override
    public boolean updateStudent(Long id, UpdateStudentRequest updateStudentRequest) {
        Student student = this.getById(id);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");
        BeanUtils.copyProperties(updateStudentRequest, student);
        return this.updateById(student);
    }

    @Override
    public StudentVo getStudentById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        Student student = this.getById(id);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");

        StudentVo studentVo = new StudentVo();
        BeanUtils.copyProperties(student, studentVo);
        return studentVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean studentTopUp(Long id, BigDecimal money) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        ThrowUtils.throwIf(money == null, ErrorCode.PARAMS_ERROR, "充值金额不能为空");

        Student student = this.getById(id);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");

        student.setBalance(student.getBalance().add(money));
        ThrowUtils.throwIf(!this.updateById(student), ErrorCode.SYSTEM_ERROR, "充值失败");

        RechargeRecord rechargeRecord = new RechargeRecord();
        rechargeRecord.setStudentId(id);
        rechargeRecord.setAmount(money);
        rechargeRecord.setCreateTime(new Date());
        ThrowUtils.throwIf(!rechargeRecordService.save(rechargeRecord), ErrorCode.SYSTEM_ERROR, "充值记录保存失败");
        return true;
    }

    @Override
    public boolean studentsPurchaseCourses(Long studentId, Long courseId) {
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        ThrowUtils.throwIf(courseId == null, ErrorCode.PARAMS_ERROR, "课程ID不能为空");

        Student student = this.getById(studentId);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");
        Course course = courseService.getById(courseId);
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");

        LambdaQueryWrapper<CourseOrder> existQuery = new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getStudentId, studentId)
                .eq(CourseOrder::getCourseId, courseId)
                .eq(CourseOrder::getStatus, 1)
                .gt(CourseOrder::getRemainingSessions, 0);
        ThrowUtils.throwIf(courseOrderService.count(existQuery) > 0,
                ErrorCode.OPERATION_ERROR, "您已购买过该课程，无需重复购买");

        BigDecimal balance = student.getBalance();
        ThrowUtils.throwIf(balance.compareTo(course.getPrice()) < 0, ErrorCode.PARAMS_ERROR, "余额不足");

        student.setBalance(balance.subtract(course.getPrice()));
        ThrowUtils.throwIf(!this.updateById(student), ErrorCode.SYSTEM_ERROR, "购买课程失败");

        Integer totalSessions = course.getTotalSessions() == null || course.getTotalSessions() <= 0
                ? 1 : course.getTotalSessions();
        CourseOrder courseOrder = new CourseOrder();
        courseOrder.setOrderNo(generateOrderNo(studentId));
        courseOrder.setStudentId(studentId);
        courseOrder.setCourseId(courseId);
        courseOrder.setCoachId(course.getCoachId());
        courseOrder.setPrice(course.getPrice());
        courseOrder.setDeliveryMode(course.getDeliveryMode());
        courseOrder.setTotalSessions(totalSessions);
        courseOrder.setRemainingSessions(totalSessions);
        courseOrder.setStatus(1);
        courseOrder.setCreateTime(new Date());
        ThrowUtils.throwIf(!courseOrderService.save(courseOrder), ErrorCode.SYSTEM_ERROR, "课程订单保存失败");

        try {
            Long coachUserId = getUserIdByCoachId(course.getCoachId());
            com.ldr.gymlink.mq.message.CourseOrderMessage message = com.ldr.gymlink.mq.message.CourseOrderMessage.builder()
                    .type(com.ldr.gymlink.mq.message.CourseOrderMessage.TYPE_NEW_ORDER)
                    .orderId(courseOrder.getId())
                    .courseId(courseId)
                    .courseName(course.getName())
                    .coachId(course.getCoachId())
                    .coachUserId(coachUserId)
                    .studentId(studentId)
                    .studentName(student.getName())
                    .price(course.getPrice())
                    .build();
            courseOrderMessageProducer.sendCourseOrderNotification(message);
        } catch (Exception e) {
            log.error("发送课程订单通知失败", e);
        }

        return true;
    }

    @Override
    public String updateStudentAvatar(Long studentId, MultipartFile file) {
        validateImageFile(file);
        File compressedImage = null;
        try {
            String uploadFilePath = "avatar/student/" + StrUtil.format("{}/{}.{}",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                    studentId,
                    "webp");
            compressedImage = compressImage(file);
            String result = cosManager.uploadFile(uploadFilePath, compressedImage);

            Student student = this.getById(studentId);
            ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");
            student.setAvatar(result);
            ThrowUtils.throwIf(!this.updateById(student), ErrorCode.SYSTEM_ERROR, "更新学员头像失败");
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
    public Page<PurchasedCourseVo> getPurchasedCourses(PurchasedCourseQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(request.getStudentId() == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");

        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 条数据");

        LambdaQueryWrapper<CourseOrder> queryWrapper = new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getStudentId, request.getStudentId());
        if (request.getCourseId() != null) {
            queryWrapper.eq(CourseOrder::getCourseId, request.getCourseId());
        }
        if (request.getCoachId() != null) {
            queryWrapper.eq(CourseOrder::getCoachId, request.getCoachId());
        }
        if (request.getStatus() != null) {
            queryWrapper.eq(CourseOrder::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(CourseOrder::getCreateTime);

        Page<CourseOrder> orderPage = courseOrderService.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<PurchasedCourseVo> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(orderPage.getTotal());
        voPage.setRecords(orderPage.getRecords().stream()
                .map(order -> buildPurchasedCourseVo(order, request))
                .filter(Objects::nonNull)
                .toList());
        return voPage;
    }

    @Override
    public List<Long> getPurchasedCourseIds(Long studentId) {
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");

        LambdaQueryWrapper<CourseOrder> queryWrapper = new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getStudentId, studentId)
                .eq(CourseOrder::getStatus, 1)
                .gt(CourseOrder::getRemainingSessions, 0)
                .select(CourseOrder::getCourseId);

        return courseOrderService.list(queryWrapper).stream()
                .map(CourseOrder::getCourseId)
                .distinct()
                .toList();
    }

    @Override
    public boolean refundCourse(Long orderId) {
        ThrowUtils.throwIf(orderId == null, ErrorCode.PARAMS_ERROR, "订单ID不能为空");

        CourseOrder order = courseOrderService.getById(orderId);
        ThrowUtils.throwIf(order == null, ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        ThrowUtils.throwIf(order.getStatus() != 1, ErrorCode.OPERATION_ERROR, "该订单当前不可申请退款");
        ThrowUtils.throwIf(order.getTotalSessions() != null
                        && order.getRemainingSessions() != null
                        && !order.getTotalSessions().equals(order.getRemainingSessions()),
                ErrorCode.OPERATION_ERROR, "课程已使用，暂不支持整单退款");

        order.setStatus(3);
        ThrowUtils.throwIf(!courseOrderService.updateById(order), ErrorCode.SYSTEM_ERROR, "申请退款失败");

        try {
            Student student = this.getById(order.getStudentId());
            Course course = courseService.getById(order.getCourseId());
            com.ldr.gymlink.mq.message.RefundMessage message = com.ldr.gymlink.mq.message.RefundMessage.builder()
                    .type(com.ldr.gymlink.mq.message.RefundMessage.TYPE_REFUND_APPLY)
                    .orderId(orderId)
                    .studentId(order.getStudentId())
                    .studentName(student != null ? student.getName() : "学员")
                    .courseName(course != null ? course.getName() : "课程")
                    .refundAmount(order.getPrice())
                    .build();
            refundMessageProducer.sendRefundNotification(message);
        } catch (Exception e) {
            log.error("发送退款申请通知失败", e);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveRefund(Long orderId) {
        ThrowUtils.throwIf(orderId == null, ErrorCode.PARAMS_ERROR, "订单ID不能为空");

        CourseOrder order = courseOrderService.getById(orderId);
        ThrowUtils.throwIf(order == null, ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        ThrowUtils.throwIf(order.getStatus() != 3, ErrorCode.OPERATION_ERROR, "该订单当前不可审批退款");

        Student student = this.getById(order.getStudentId());
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");

        BigDecimal refundAmount = order.getPrice();
        student.setBalance(student.getBalance().add(refundAmount));
        ThrowUtils.throwIf(!this.updateById(student), ErrorCode.SYSTEM_ERROR, "退款失败，更新学员余额失败");

        order.setStatus(2);
        ThrowUtils.throwIf(!courseOrderService.updateById(order), ErrorCode.SYSTEM_ERROR, "退款失败，更新订单状态失败");

        try {
            Course course = courseService.getById(order.getCourseId());
            Long studentUserId = getUserIdByStudentId(order.getStudentId());
            com.ldr.gymlink.mq.message.RefundMessage message = com.ldr.gymlink.mq.message.RefundMessage.builder()
                    .type(com.ldr.gymlink.mq.message.RefundMessage.TYPE_REFUND_APPROVED)
                    .orderId(orderId)
                    .studentId(order.getStudentId())
                    .studentUserId(studentUserId)
                    .studentName(student.getName())
                    .courseName(course != null ? course.getName() : "课程")
                    .refundAmount(refundAmount)
                    .build();
            refundMessageProducer.sendRefundNotification(message);
        } catch (Exception e) {
            log.error("发送退款通过通知失败", e);
        }

        return true;
    }

    @Override
    public boolean rejectRefund(Long orderId) {
        ThrowUtils.throwIf(orderId == null, ErrorCode.PARAMS_ERROR, "订单ID不能为空");

        CourseOrder order = courseOrderService.getById(orderId);
        ThrowUtils.throwIf(order == null, ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        ThrowUtils.throwIf(order.getStatus() != 3, ErrorCode.OPERATION_ERROR, "该订单当前不可拒绝退款");

        order.setStatus(1);
        ThrowUtils.throwIf(!courseOrderService.updateById(order), ErrorCode.SYSTEM_ERROR, "操作失败");

        try {
            Student student = this.getById(order.getStudentId());
            Course course = courseService.getById(order.getCourseId());
            Long studentUserId = getUserIdByStudentId(order.getStudentId());
            com.ldr.gymlink.mq.message.RefundMessage message = com.ldr.gymlink.mq.message.RefundMessage.builder()
                    .type(com.ldr.gymlink.mq.message.RefundMessage.TYPE_REFUND_REJECTED)
                    .orderId(orderId)
                    .studentId(order.getStudentId())
                    .studentUserId(studentUserId)
                    .studentName(student != null ? student.getName() : "学员")
                    .courseName(course != null ? course.getName() : "课程")
                    .refundAmount(order.getPrice())
                    .build();
            refundMessageProducer.sendRefundNotification(message);
        } catch (Exception e) {
            log.error("发送退款拒绝通知失败", e);
        }

        return true;
    }

    @Override
    public Page<PurchasedCourseVo> getRefundOrders(PurchasedCourseQueryRequest request) {
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();

        LambdaQueryWrapper<CourseOrder> queryWrapper = new LambdaQueryWrapper<>();
        if (request.getStudentId() != null) {
            queryWrapper.eq(CourseOrder::getStudentId, request.getStudentId());
        }
        if (request.getCourseId() != null) {
            queryWrapper.eq(CourseOrder::getCourseId, request.getCourseId());
        }
        if (request.getCoachId() != null) {
            queryWrapper.eq(CourseOrder::getCoachId, request.getCoachId());
        }
        if (request.getStatus() != null) {
            queryWrapper.eq(CourseOrder::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(CourseOrder::getCreateTime);

        Page<CourseOrder> orderPage = courseOrderService.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<PurchasedCourseVo> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(orderPage.getTotal());
        voPage.setRecords(orderPage.getRecords().stream()
                .map(order -> buildRefundOrderVo(order, request))
                .filter(Objects::nonNull)
                .toList());
        return voPage;
    }

    private PurchasedCourseVo buildPurchasedCourseVo(CourseOrder order, PurchasedCourseQueryRequest request) {
        PurchasedCourseVo vo = new PurchasedCourseVo();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setCourseId(order.getCourseId());
        vo.setCoachId(order.getCoachId());
        vo.setPrice(order.getPrice());
        vo.setDeliveryMode(order.getDeliveryMode());
        vo.setTotalSessions(order.getTotalSessions());
        vo.setRemainingSessions(order.getRemainingSessions());
        vo.setPurchaseTime(order.getCreateTime());
        vo.setStatus(order.getStatus());

        Course course = courseService.getById(order.getCourseId());
        if (course != null) {
            if (StrUtil.isNotBlank(request.getCourseName()) && !course.getName().contains(request.getCourseName())) {
                return null;
            }
            vo.setCourseName(course.getName());
            vo.setCourseImage(course.getImage());
            vo.setCourseType(course.getType());
            vo.setDifficulty(course.getDifficulty());
            vo.setDuration(course.getDuration());
        }

        if (order.getCoachId() != null) {
            Coach coach = coachMapper.selectById(order.getCoachId());
            if (coach != null) {
                vo.setCoachName(coach.getName());
            }
        }
        return vo;
    }

    private PurchasedCourseVo buildRefundOrderVo(CourseOrder order, PurchasedCourseQueryRequest request) {
        PurchasedCourseVo vo = buildPurchasedCourseVo(order, request);
        if (vo == null) {
            return null;
        }
        Student student = this.getById(order.getStudentId());
        if (student != null) {
            vo.setStudentName(student.getName());
        }
        return vo;
    }

    private String generateOrderNo(Long studentId) {
        return "ORDER_" + studentId + "_" + System.currentTimeMillis();
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

    private Long getUserIdByStudentId(Long studentId) {
        if (studentId == null) {
            return null;
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getAssociatedUserId, studentId)
                .eq(User::getRole, UserRoleEnum.STUDENT.getValue());
        User user = userService.getOne(queryWrapper);
        return user != null ? user.getId() : null;
    }

    private Long getUserIdByCoachId(Long coachId) {
        if (coachId == null) {
            return null;
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getAssociatedUserId, coachId)
                .eq(User::getRole, UserRoleEnum.COACH.getValue());
        User user = userService.getOne(queryWrapper);
        return user != null ? user.getId() : null;
    }
}

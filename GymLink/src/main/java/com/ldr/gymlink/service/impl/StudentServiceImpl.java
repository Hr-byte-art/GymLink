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
import com.ldr.gymlink.mapper.StudentMapper;
import com.ldr.gymlink.model.dto.student.AddStudentRequest;
import com.ldr.gymlink.model.dto.student.StudentQueryPageRequest;
import com.ldr.gymlink.model.dto.student.UpdateStudentRequest;
import com.ldr.gymlink.model.entity.*;
import com.ldr.gymlink.model.enums.UserRoleEnum;
import com.ldr.gymlink.model.vo.StudentVo;
import com.ldr.gymlink.service.*;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * @author 木子宸
 * @description 针对表【student(学员/用户信息表)】的数据库操作Service实现
 * @createDate 2025-11-29 20:39:16
 */
@Service
@Slf4j
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

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

    /**
     * 最大文件大小（5MB）
     */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 允许的图片格式
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");


    @Override
    public StudentVo getStudentByUserId(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户id不能为空");
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        String role = user.getRole();
        StudentVo studentVo = new StudentVo();
        if (UserRoleEnum.STUDENT.getValue().equals(role)) {
            LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<Student>().eq(Student::getId, user.getAssociatedUserId());
            Student student = this.getOne(queryWrapper);
            ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员信息不存在");
            BeanUtils.copyProperties(student, studentVo);
        }
        return studentVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentVo addStudent(AddStudentRequest addStudentRequest) {
        String password = addStudentRequest.getPassword();
        String string = userService.encryptPassword(password);
        addStudentRequest.setPassword(string);
        Student student = new Student();
        BeanUtils.copyProperties(addStudentRequest, student);
        student.setCreateTime(new Date());
        boolean save = this.save(student);

        User user = new User();
        user.setUsername(addStudentRequest.getUsername());
        user.setPassword(addStudentRequest.getPassword());
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setIsDelete(0);
        user.setRole(UserRoleEnum.STUDENT.getValue());
        user.setAssociatedUserId(student.getId());

        boolean save1 = userService.save(user);
        if (!save || !save1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败，请稍后重试");
        }
        StudentVo studentVo = new StudentVo();
        BeanUtils.copyProperties(student, studentVo);
        return studentVo;
    }

    @Override
    public Page<StudentVo> listStudentPage(StudentQueryPageRequest studentQueryPageRequest) {
        ThrowUtils.throwIf(studentQueryPageRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int pageSize = studentQueryPageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
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
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "学员id不能为空");
        Student student = this.getById(id);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");

        // 级联删除：删除关联的 User 记录
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<User>().eq(User::getAssociatedUserId, id).eq(User::getRole, UserRoleEnum.STUDENT.getValue());
        userService.remove(userQueryWrapper);

        // 删除学员记录
        return this.removeById(id);
    }

    @Override
    public boolean updateStudent(Long id, UpdateStudentRequest updateStudentRequest) {
        Student student = this.getById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        BeanUtils.copyProperties(updateStudentRequest, student);
        return this.updateById(student);
    }

    @Override
    public StudentVo getStudentById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "学员id不能为空");
        Student student = this.getById(id);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");
        StudentVo studentVo = new StudentVo();
        BeanUtils.copyProperties(student, studentVo);
        return studentVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean studentTopUp(Long id, BigDecimal money) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "学员id不能为空");
        ThrowUtils.throwIf(money == null, ErrorCode.PARAMS_ERROR, "充值金额不能为空");
        Student student = this.getById(id);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");
        student.setBalance(student.getBalance().add(money));
        boolean b = this.updateById(student);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "充值失败，请稍后重试");
        }
        RechargeRecord rechargeRecord = new RechargeRecord();
        rechargeRecord.setStudentId(id);
        rechargeRecord.setAmount(money);
        rechargeRecord.setCreateTime(new Date());
        boolean save = rechargeRecordService.save(rechargeRecord);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "充值记录保存失败，请稍后重试");
        }
        return true;
    }

    @Override
    public boolean studentsPurchaseCourses(Long studentId, Long courseId) {
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员id不能为空");
        ThrowUtils.throwIf(courseId == null, ErrorCode.PARAMS_ERROR, "课程id不能为空");
        Student student = this.getById(studentId);
        ThrowUtils.throwIf(student == null, ErrorCode.NOT_FOUND_ERROR, "学员不存在");
        Course course = courseService.getById(courseId);
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        // 检查余额是否组狗
        BigDecimal balance = student.getBalance();
        if (balance.compareTo(course.getPrice()) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "余额不足");
        }
        student.setBalance(balance.subtract(course.getPrice()));
        boolean b = this.updateById(student);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "购买课程失败，请稍后重试");
        }
        CourseOrder courseOrder = new CourseOrder();
        courseOrder.setOrderNo(generateOrderNo(studentId));
        courseOrder.setStudentId(studentId);
        courseOrder.setCourseId(courseId);
        courseOrder.setCoachId(course.getCoachId());
        courseOrder.setPrice(course.getPrice());
        courseOrder.setStatus(1);
        courseOrder.setCreateTime(new Date());
        boolean save = courseOrderService.save(courseOrder);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "课程订单保存失败，请稍后重试");
        }
        return true;
    }

    @Override
    public String updateStudentAvatar(Long studentId, MultipartFile file) {

        validateImageFile(file);
        File compressedImage = null;
        try {
            String uploadFilePath = "avatar/student/" + StrUtil.format("{}/{}.{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")), studentId, "webp");

            compressedImage = compressImage(file);

            String result = cosManager.uploadFile(uploadFilePath, compressedImage);

            // 9. 更新数据库中的头像URL
            Student student = this.getById(studentId);
            if (student == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            }
            student.setAvatar(result);
            boolean update = this.updateById(student);
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

    // 计算订单号 bound
    private String generateOrderNo(Long studentId) {
        return "ORDER_" + studentId + "_" + System.currentTimeMillis();
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
}
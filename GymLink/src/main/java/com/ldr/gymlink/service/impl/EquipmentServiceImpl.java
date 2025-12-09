package com.ldr.gymlink.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.manager.CosManager;
import com.ldr.gymlink.mapper.EquipmentMapper;
import com.ldr.gymlink.model.dto.equipment.AddEquipmentRequest;
import com.ldr.gymlink.model.dto.equipment.AllEquipmentReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.BookingEquipmentRequest;
import com.ldr.gymlink.model.dto.equipment.EquipmentQueryPageRequest;
import com.ldr.gymlink.model.dto.equipment.EquipmentReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.StudentEquipmentReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.StudentEquipmentTimeRangeReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.EquipmentTimeRangeReservationQueryRequest;
import com.ldr.gymlink.model.dto.equipment.UpdateEquipmentRequest;
import com.ldr.gymlink.model.entity.Equipment;
import com.ldr.gymlink.model.entity.EquipmentReservation;
import com.ldr.gymlink.model.vo.EquipmentReservationVo;
import com.ldr.gymlink.model.vo.EquipmentVo;
import com.ldr.gymlink.model.vo.StudentVo;
import com.ldr.gymlink.service.EquipmentReservationService;
import com.ldr.gymlink.service.EquipmentService;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.utils.ThrowUtils;
import com.luciad.imageio.webp.WebPWriteParam;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.ldr.gymlink.model.vo.EquipmentStatisticsVo;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 木子宸
 * @description 针对表【equipment(健身器材表)】的数据库操作Service实现
 * @createDate 2025-11-30 21:36:00
 */
@Service
@Slf4j
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment>
        implements EquipmentService {

    @Resource
    private EquipmentReservationService equipmentReservationService;

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
    @Transactional(rollbackFor = Exception.class)
    public EquipmentVo addEquipment(AddEquipmentRequest addEquipmentRequest) {
        ThrowUtils.throwIf(addEquipmentRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        Equipment equipment = new Equipment();
        BeanUtils.copyProperties(addEquipmentRequest, equipment);
        equipment.setCreateTime(new Date());
        boolean save = this.save(equipment);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加健身器材失败，请稍后重试");
        }

        EquipmentVo equipmentVo = new EquipmentVo();
        BeanUtils.copyProperties(equipment, equipmentVo);
        return equipmentVo;
    }

    @Override
    public Page<EquipmentVo> listEquipmentPage(EquipmentQueryPageRequest equipmentQueryPageRequest) {
        ThrowUtils.throwIf(equipmentQueryPageRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int pageSize = equipmentQueryPageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个器材");
        int pageNum = equipmentQueryPageRequest.getPageNum();

        LambdaQueryWrapper<Equipment> queryWrapper = getEquipmentQueryWrapper(equipmentQueryPageRequest);
        Page<Equipment> page = this.page(Page.of(pageNum, pageSize), queryWrapper);

        Page<EquipmentVo> equipmentVoPage = new Page<>(pageNum, pageSize);
        equipmentVoPage.setTotal(page.getTotal());
        equipmentVoPage.setRecords(page.getRecords().stream().map(equipment -> {
            EquipmentVo equipmentVo = new EquipmentVo();
            BeanUtils.copyProperties(equipment, equipmentVo);
            return equipmentVo;
        }).toList());
        return equipmentVoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEquipment(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "健身器材id不能为空");
        Equipment equipment = this.getById(id);
        ThrowUtils.throwIf(equipment == null, ErrorCode.NOT_FOUND_ERROR, "健身器材不存在");

        // 删除健身器材记录
        return this.removeById(id);
    }

    @Override
    public boolean updateEquipment(Long id, UpdateEquipmentRequest updateEquipmentRequest) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "健身器材id不能为空");
        Equipment equipment = this.getById(id);
        if (equipment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "健身器材不存在");
        }
        BeanUtils.copyProperties(updateEquipmentRequest, equipment);
        return this.updateById(equipment);
    }

    @Override
    public EquipmentVo getEquipmentById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "健身器材id不能为空");
        Equipment equipment = this.getById(id);
        ThrowUtils.throwIf(equipment == null, ErrorCode.NOT_FOUND_ERROR, "健身器材不存在");
        EquipmentVo equipmentVo = new EquipmentVo();
        BeanUtils.copyProperties(equipment, equipmentVo);
        return equipmentVo;
    }

    @Override
    public boolean bookingEquipment(BookingEquipmentRequest bookingEquipmentRequest) {

        ThrowUtils.throwIf(bookingEquipmentRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long equipmentId = bookingEquipmentRequest.getEquipmentId();
        Long studentId = bookingEquipmentRequest.getStudentId();
        Date startTime = bookingEquipmentRequest.getStartTime();
        Date endTime = bookingEquipmentRequest.getEndTime();

        // 检查当前器材是否被预约
        LambdaQueryWrapper<EquipmentReservation> wrapper = new LambdaQueryWrapper<EquipmentReservation>()
                .eq(EquipmentReservation::getEquipmentId, equipmentId)
                .eq(EquipmentReservation::getStatus, 1);
        if (equipmentReservationService.count(wrapper) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前器材已被预约");
        }

        // 校验用户的所选时间段内是否预约了其余的器材

        LambdaQueryWrapper<EquipmentReservation> wrapper1 = new LambdaQueryWrapper<EquipmentReservation>()
                .eq(EquipmentReservation::getStudentId, studentId)
                .between(EquipmentReservation::getStartTime, startTime, endTime)
                .eq(EquipmentReservation::getStatus, 1);
        long count = equipmentReservationService.count(wrapper1);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前用户已预约其他器材");
        }

        // 检查用户当天是否预约了超过4个器材
        LambdaQueryWrapper<EquipmentReservation> wrapper2 = new LambdaQueryWrapper<EquipmentReservation>()
                .eq(EquipmentReservation::getStudentId, studentId)
                .eq(EquipmentReservation::getStatus, 1)
                .apply("DATE(create_time) = DATE(NOW())");
        long count1 = equipmentReservationService.count(wrapper2);
        if (count1 >= 4) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前用户已预约超过4个器材");
        }

        EquipmentReservation equipmentReservation = new EquipmentReservation();
        equipmentReservation.setStudentId(studentId);
        equipmentReservation.setEquipmentId(equipmentId);
        equipmentReservation.setStartTime(startTime);
        equipmentReservation.setEndTime(endTime);
        equipmentReservation.setStatus(1);
        equipmentReservation.setCreateTime(new Date());
        return equipmentReservationService.save(equipmentReservation);
    }

    @Resource
    private StudentService studentService;

    @Override
    public boolean cancelBookingEquipment(Long bookingId) {
        ThrowUtils.throwIf(bookingId == null, ErrorCode.PARAMS_ERROR, "预约id不能为空");
        EquipmentReservation equipmentReservation = equipmentReservationService.getById(bookingId);
        ThrowUtils.throwIf(equipmentReservation == null, ErrorCode.NOT_FOUND_ERROR, "预约记录不存在");
        return equipmentReservationService.removeById(bookingId);
    }

    @Override
    public Page<EquipmentReservationVo> listStudentReservationPage(
            StudentEquipmentReservationQueryRequest studentEquipmentReservationQueryRequest) {
        ThrowUtils.throwIf(studentEquipmentReservationQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long studentId = studentEquipmentReservationQueryRequest.getStudentId();
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");

        int pageSize = studentEquipmentReservationQueryRequest.getPageSize();
        int pageNum = studentEquipmentReservationQueryRequest.getPageNum();

        LambdaQueryWrapper<EquipmentReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EquipmentReservation::getStudentId, studentId);
        if (studentEquipmentReservationQueryRequest.getStatus() != null) {
            queryWrapper.eq(EquipmentReservation::getStatus, studentEquipmentReservationQueryRequest.getStatus());
        }
        queryWrapper.orderByDesc(EquipmentReservation::getCreateTime);

        Page<EquipmentReservation> page = equipmentReservationService.page(Page.of(pageNum, pageSize), queryWrapper);
        return getEquipmentReservationVoPage(page);
    }

    @Override
    public Page<EquipmentReservationVo> listAllReservationPage(
            AllEquipmentReservationQueryRequest allEquipmentReservationQueryRequest) {
        ThrowUtils.throwIf(allEquipmentReservationQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        int pageSize = allEquipmentReservationQueryRequest.getPageSize();
        int pageNum = allEquipmentReservationQueryRequest.getPageNum();

        LambdaQueryWrapper<EquipmentReservation> queryWrapper = new LambdaQueryWrapper<>();
        if (allEquipmentReservationQueryRequest.getStudentId() != null) {
            queryWrapper.eq(EquipmentReservation::getStudentId, allEquipmentReservationQueryRequest.getStudentId());
        }
        if (allEquipmentReservationQueryRequest.getEquipmentId() != null) {
            queryWrapper.eq(EquipmentReservation::getEquipmentId, allEquipmentReservationQueryRequest.getEquipmentId());
        }
        if (allEquipmentReservationQueryRequest.getStatus() != null) {
            queryWrapper.eq(EquipmentReservation::getStatus, allEquipmentReservationQueryRequest.getStatus());
        }
        queryWrapper.orderByDesc(EquipmentReservation::getCreateTime);

        Page<EquipmentReservation> page = equipmentReservationService.page(Page.of(pageNum, pageSize), queryWrapper);
        return getEquipmentReservationVoPage(page);
    }

    @Override
    public Page<EquipmentReservationVo> listReservationsByEquipment(EquipmentReservationQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long equipmentId = request.getEquipmentId();
        ThrowUtils.throwIf(equipmentId == null, ErrorCode.PARAMS_ERROR, "器材ID不能为空");

        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        LambdaQueryWrapper<EquipmentReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EquipmentReservation::getEquipmentId, equipmentId);
        if (request.getStatus() != null) {
            queryWrapper.eq(EquipmentReservation::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(EquipmentReservation::getCreateTime);

        Page<EquipmentReservation> page = equipmentReservationService.page(Page.of(pageNum, pageSize), queryWrapper);
        return getEquipmentReservationVoPage(page);
    }

    @Override
    public Page<EquipmentReservationVo> listReservationsByTimeRange(EquipmentTimeRangeReservationQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        ThrowUtils.throwIf(startTime == null || endTime == null, ErrorCode.PARAMS_ERROR, "时间段不能为空");

        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        LambdaQueryWrapper<EquipmentReservation> queryWrapper = new LambdaQueryWrapper<>();
        // 查询时间段有交集的预约： reservation.startTime < request.endTime AND reservation.endTime
        // > request.startTime
        queryWrapper.lt(EquipmentReservation::getStartTime, endTime);
        queryWrapper.gt(EquipmentReservation::getEndTime, startTime);

        if (request.getStatus() != null) {
            queryWrapper.eq(EquipmentReservation::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(EquipmentReservation::getCreateTime);

        Page<EquipmentReservation> page = equipmentReservationService.page(Page.of(pageNum, pageSize), queryWrapper);
        return getEquipmentReservationVoPage(page);
    }

    @Override
    public Page<EquipmentReservationVo> listStudentReservationsByTimeRange(
            StudentEquipmentTimeRangeReservationQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long studentId = request.getStudentId();
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员ID不能为空");
        ThrowUtils.throwIf(startTime == null || endTime == null, ErrorCode.PARAMS_ERROR, "时间段不能为空");

        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        LambdaQueryWrapper<EquipmentReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EquipmentReservation::getStudentId, studentId);
        queryWrapper.lt(EquipmentReservation::getStartTime, endTime);
        queryWrapper.gt(EquipmentReservation::getEndTime, startTime);

        if (request.getStatus() != null) {
            queryWrapper.eq(EquipmentReservation::getStatus, request.getStatus());
        }
        queryWrapper.orderByDesc(EquipmentReservation::getCreateTime);

        Page<EquipmentReservation> page = equipmentReservationService.page(Page.of(pageNum, pageSize), queryWrapper);
        return getEquipmentReservationVoPage(page);
    }

    private Page<EquipmentReservationVo> getEquipmentReservationVoPage(Page<EquipmentReservation> page) {
        Page<EquipmentReservationVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<EquipmentReservationVo> voList = page.getRecords().stream().map(reservation -> {
            EquipmentReservationVo vo = new EquipmentReservationVo();
            BeanUtils.copyProperties(reservation, vo);
            // 填充器材名称
            Equipment equipment = this.getById(reservation.getEquipmentId());
            if (equipment != null) {
                vo.setEquipmentName(equipment.getName());
            }
            // 填充学员姓名
            StudentVo student = studentService.getStudentById(reservation.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getName());
            }
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 获取健身器材查询条件
     *
     * @param equipmentQueryPageRequest 查询条件
     * @return 查询条件
     */
    private LambdaQueryWrapper<Equipment> getEquipmentQueryWrapper(
            EquipmentQueryPageRequest equipmentQueryPageRequest) {
        LambdaQueryWrapper<Equipment> queryWrapper = new LambdaQueryWrapper<>();
        if (equipmentQueryPageRequest == null) {
            return queryWrapper;
        }

        String name = equipmentQueryPageRequest.getName();
        String location = equipmentQueryPageRequest.getLocation();
        Integer status = equipmentQueryPageRequest.getStatus();
        String type = equipmentQueryPageRequest.getType();
        // 模糊查询器材名称
        queryWrapper.like(StringUtils.isNotBlank(name), Equipment::getName, name);
        // 模糊查询放置位置
        queryWrapper.like(StringUtils.isNotBlank(location), Equipment::getLocation, location);
        // 精确查询状态
        queryWrapper.eq(status != null, Equipment::getStatus, status);
        // 精确查询类型（只有当 type 不为空时才添加条件）
        queryWrapper.eq(StringUtils.isNotBlank(type), Equipment::getType, type);
        return queryWrapper;
    }

    @Override
    public String updateEquipmentImage(Long equipmentId, MultipartFile file) {
        validateImageFile(file);
        File compressedImage = null;
        try {
            String uploadFilePath = "equipment/" + StrUtil.format("{}/{}.{}", 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")), 
                    equipmentId, "webp");

            compressedImage = compressImage(file);
            String result = cosManager.uploadFile(uploadFilePath, compressedImage);

            // 更新数据库中的图片URL
            Equipment equipment = this.getById(equipmentId);
            if (equipment == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "器材不存在");
            }
            equipment.setImage(result);
            boolean update = this.updateById(equipment);
            if (!update) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新器材图片失败");
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (compressedImage != null && compressedImage.exists()) {
                compressedImage.delete();
            }
        }
    }

    /**
     * 校验图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过5MB");
        }
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

    /**
     * 将图片转换为 WebP 格式
     */
    private void convertImage2Webp(File oldFile, File newFile, float quality) throws IOException {
        ImageWriter writer = null;
        FileImageOutputStream output = null;

        try {
            BufferedImage image = ImageIO.read(oldFile);
            if (image == null) {
                throw new IOException("无法读取图片文件，可能格式不支持");
            }
            writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
            WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
            writeParam.setCompressionMode(WebPWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionType(writeParam.getCompressionTypes()[0]);
            writeParam.setCompressionQuality(quality);
            output = new FileImageOutputStream(newFile);
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), writeParam);
            log.info("图片压缩成功，原始大小: {} KB, 压缩后大小: {} KB",
                    oldFile.length() / 1024, newFile.length() / 1024);
        } catch (IOException e) {
            log.error("图片转换为 WebP 失败", e);
            throw e;
        } finally {
            if (output != null) {
                try { output.close(); } catch (IOException e) { log.error("关闭输出流失败", e); }
            }
            if (writer != null) {
                writer.dispose();
            }
        }
    }

    @Override
    public EquipmentStatisticsVo getEquipmentStatistics() {
        EquipmentStatisticsVo vo = new EquipmentStatisticsVo();

        // 1. 器材总数
        vo.setTotalEquipment(this.count());

        // 2. 正常状态器材数
        vo.setNormalCount(this.count(new LambdaQueryWrapper<Equipment>().eq(Equipment::getStatus, 1)));

        // 3. 维护中器材数
        vo.setMaintenanceCount(this.count(new LambdaQueryWrapper<Equipment>().eq(Equipment::getStatus, 2)));

        // 4. 今日预约数
        vo.setTodayReservationCount(equipmentReservationService.count(
                new LambdaQueryWrapper<EquipmentReservation>().apply("DATE(create_time) = CURDATE()")));

        // 5. 本周预约数
        vo.setWeekReservationCount(equipmentReservationService.count(
                new LambdaQueryWrapper<EquipmentReservation>().apply("YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1)")));

        // 6. 本月预约数
        vo.setMonthReservationCount(equipmentReservationService.count(
                new LambdaQueryWrapper<EquipmentReservation>().apply("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')")));

        // 7. 各类型器材数量统计
        List<Equipment> allEquipment = this.list();
        Map<String, Long> typeCountMap = allEquipment.stream()
                .filter(e -> StringUtils.isNotBlank(e.getType()))
                .collect(Collectors.groupingBy(Equipment::getType, Collectors.counting()));
        List<EquipmentStatisticsVo.TypeCountVo> typeStatistics = typeCountMap.entrySet().stream()
                .map(entry -> {
                    EquipmentStatisticsVo.TypeCountVo typeVo = new EquipmentStatisticsVo.TypeCountVo();
                    typeVo.setType(entry.getKey());
                    typeVo.setTypeName(getTypeName(entry.getKey()));
                    typeVo.setCount(entry.getValue());
                    return typeVo;
                }).collect(Collectors.toList());
        vo.setTypeStatistics(typeStatistics);

        // 8. 最近7天每日预约趋势
        List<EquipmentStatisticsVo.DailyCountVo> dailyTrend = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String date = LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            long count = equipmentReservationService.count(
                    new LambdaQueryWrapper<EquipmentReservation>().apply("DATE(create_time) = '" + date + "'"));
            EquipmentStatisticsVo.DailyCountVo dailyVo = new EquipmentStatisticsVo.DailyCountVo();
            dailyVo.setDate(date);
            dailyVo.setCount(count);
            dailyTrend.add(dailyVo);
        }
        vo.setDailyReservationTrend(dailyTrend);

        // 9. 热门器材TOP10
        List<EquipmentReservation> allReservations = equipmentReservationService.list();
        Map<Long, Long> equipmentReservationCount = allReservations.stream()
                .collect(Collectors.groupingBy(EquipmentReservation::getEquipmentId, Collectors.counting()));
        List<EquipmentStatisticsVo.EquipmentRankVo> hotRank = equipmentReservationCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    EquipmentStatisticsVo.EquipmentRankVo rankVo = new EquipmentStatisticsVo.EquipmentRankVo();
                    rankVo.setEquipmentId(entry.getKey());
                    Equipment equipment = this.getById(entry.getKey());
                    rankVo.setEquipmentName(equipment != null ? equipment.getName() : "未知器材");
                    rankVo.setReservationCount(entry.getValue());
                    return rankVo;
                }).collect(Collectors.toList());
        vo.setHotEquipmentRank(hotRank);

        return vo;
    }

    /**
     * 获取器材类型名称
     */
    private String getTypeName(String type) {
        Map<String, String> typeNameMap = Map.ofEntries(
                Map.entry("1", "有氧健身器材"), Map.entry("1-1", "跑步机"), Map.entry("1-2", "椭圆机"),
                Map.entry("1-3", "动感单车"), Map.entry("1-4", "划船机"), Map.entry("1-5", "健身车"),
                Map.entry("1-6", "楼梯机"), Map.entry("1-7", "体适能运动机"),
                Map.entry("2", "力量训练器材"), Map.entry("2-1", "固定器械"), Map.entry("2-2", "自由重量器材"),
                Map.entry("2-3", "综合训练器材"), Map.entry("3", "功能性训练器材"), Map.entry("4", "小型健身器械"),
                Map.entry("5", "康复与辅助器材"), Map.entry("6", "其他辅助设备"),
                Map.entry("7", "商用专用器材"), Map.entry("8", "家用专用器材")
        );
        return typeNameMap.getOrDefault(type, type);
    }
}

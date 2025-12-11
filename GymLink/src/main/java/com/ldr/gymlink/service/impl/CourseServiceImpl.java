package com.ldr.gymlink.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.manager.CosManager;
import com.ldr.gymlink.model.dto.course.AddCourseRequest;
import com.ldr.gymlink.model.dto.course.CourseQueryPageRequest;
import com.ldr.gymlink.model.dto.course.UpdateCourseRequest;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.Course;
import com.ldr.gymlink.model.entity.CourseOrder;
import com.ldr.gymlink.model.vo.CourseStatisticsVo;
import com.ldr.gymlink.model.vo.CourseVo;
import com.ldr.gymlink.service.CourseService;
import com.ldr.gymlink.mapper.CoachMapper;
import com.ldr.gymlink.mapper.CourseMapper;
import com.ldr.gymlink.mapper.CourseOrderMapper;
import com.ldr.gymlink.utils.ThrowUtils;
import com.luciad.imageio.webp.WebPWriteParam;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 木子宸
 * @description 针对表【course(健身课程表)】的数据库操作Service实现
 * @createDate 2025-11-30 20:57:06
 */
@Slf4j
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course>
        implements CourseService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    @jakarta.annotation.Resource
    private CourseOrderMapper courseOrderMapper;

    @jakarta.annotation.Resource
    private CoachMapper coachMapper;

    @jakarta.annotation.Resource
    private CosManager cosManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseVo addCourse(AddCourseRequest addCourseRequest) {
        ThrowUtils.throwIf(addCourseRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        Course course = new Course();
        BeanUtils.copyProperties(addCourseRequest, course);
        course.setCreateTime(new Date());
        boolean save = this.save(course);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加课程失败，请稍后重试");
        }

        CourseVo courseVo = new CourseVo();
        BeanUtils.copyProperties(course, courseVo);
        return courseVo;
    }

    @Override
    public Page<CourseVo> listCoursePage(CourseQueryPageRequest courseQueryPageRequest) {
        ThrowUtils.throwIf(courseQueryPageRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int pageSize = courseQueryPageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个课程");
        int pageNum = courseQueryPageRequest.getPageNum();

        LambdaQueryWrapper<Course> queryWrapper = getCourseQueryWrapper(courseQueryPageRequest);
        Page<Course> page = this.page(Page.of(pageNum, pageSize), queryWrapper);

        // 获取所有教练ID并批量查询教练信息
        List<Long> coachIds = page.getRecords().stream()
                .map(Course::getCoachId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> coachNameMap = new HashMap<>();
        if (!coachIds.isEmpty()) {
            List<Coach> coaches = coachMapper.selectBatchIds(coachIds);
            coachNameMap = coaches.stream()
                    .collect(Collectors.toMap(Coach::getId, Coach::getName, (a, b) -> a));
        }

        Map<Long, String> finalCoachNameMap = coachNameMap;
        Page<CourseVo> courseVoPage = new Page<>(pageNum, pageSize);
        courseVoPage.setTotal(page.getTotal());
        courseVoPage.setRecords(page.getRecords().stream().map(course -> {
            CourseVo courseVo = new CourseVo();
            BeanUtils.copyProperties(course, courseVo);
            // 填充教练姓名
            if (course.getCoachId() != null) {
                courseVo.setCoachName(finalCoachNameMap.get(course.getCoachId()));
            }
            return courseVo;
        }).toList());
        return courseVoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourse(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "课程id不能为空");
        Course course = this.getById(id);
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        // 删除课程记录
        return this.removeById(id);
    }

    @Override
    public boolean updateCourse(Long id, UpdateCourseRequest updateCourseRequest) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "课程id不能为空");
        Course course = this.getById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }
        BeanUtils.copyProperties(updateCourseRequest, course);
        return this.updateById(course);
    }

    @Override
    public CourseVo getCourseById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "课程id不能为空");
        Course course = this.getById(id);
        ThrowUtils.throwIf(course == null, ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        CourseVo courseVo = new CourseVo();
        BeanUtils.copyProperties(course, courseVo);
        return courseVo;
    }

    @Override
    public CourseStatisticsVo getCourseStatistics() {
        CourseStatisticsVo vo = new CourseStatisticsVo();

        // 1. 课程总数
        vo.setTotalCourse(this.count());

        // 2. 获取时间范围
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

        // 3. 订单统计（只统计已支付的订单 status=1）
        LambdaQueryWrapper<CourseOrder> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(CourseOrder::getStatus, 1).ge(CourseOrder::getCreateTime, todayStart);
        vo.setTodayOrderCount(courseOrderMapper.selectCount(todayQuery));

        LambdaQueryWrapper<CourseOrder> weekQuery = new LambdaQueryWrapper<>();
        weekQuery.eq(CourseOrder::getStatus, 1).ge(CourseOrder::getCreateTime, weekStart);
        vo.setWeekOrderCount(courseOrderMapper.selectCount(weekQuery));

        LambdaQueryWrapper<CourseOrder> monthQuery = new LambdaQueryWrapper<>();
        monthQuery.eq(CourseOrder::getStatus, 1).ge(CourseOrder::getCreateTime, monthStart);
        vo.setMonthOrderCount(courseOrderMapper.selectCount(monthQuery));

        // 4. 销售额统计
        LambdaQueryWrapper<CourseOrder> allPaidQuery = new LambdaQueryWrapper<>();
        allPaidQuery.eq(CourseOrder::getStatus, 1);
        List<CourseOrder> allPaidOrders = courseOrderMapper.selectList(allPaidQuery);
        BigDecimal totalRevenue = allPaidOrders.stream()
                .map(CourseOrder::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalRevenue(totalRevenue);

        LambdaQueryWrapper<CourseOrder> monthPaidQuery = new LambdaQueryWrapper<>();
        monthPaidQuery.eq(CourseOrder::getStatus, 1).ge(CourseOrder::getCreateTime, monthStart);
        List<CourseOrder> monthPaidOrders = courseOrderMapper.selectList(monthPaidQuery);
        BigDecimal monthRevenue = monthPaidOrders.stream()
                .map(CourseOrder::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setMonthRevenue(monthRevenue);

        // 5. 难度分布统计
        List<Course> allCourses = this.list();
        Map<String, Long> difficultyMap = allCourses.stream()
                .filter(c -> StringUtils.isNotBlank(c.getDifficulty()))
                .collect(Collectors.groupingBy(Course::getDifficulty, Collectors.counting()));
        Map<String, String> difficultyNameMap = Map.of(
                "beginner", "初级",
                "intermediate", "中级",
                "advanced", "高级"
        );
        List<CourseStatisticsVo.DifficultyCountVo> difficultyStats = difficultyMap.entrySet().stream()
                .map(entry -> {
                    CourseStatisticsVo.DifficultyCountVo dv = new CourseStatisticsVo.DifficultyCountVo();
                    dv.setDifficulty(entry.getKey());
                    dv.setDifficultyName(difficultyNameMap.getOrDefault(entry.getKey(), entry.getKey()));
                    dv.setCount(entry.getValue());
                    return dv;
                }).collect(Collectors.toList());
        vo.setDifficultyStatistics(difficultyStats);

        // 6. 类型分布统计
        Map<String, Long> typeMap = allCourses.stream()
                .filter(c -> StringUtils.isNotBlank(c.getType()))
                .collect(Collectors.groupingBy(Course::getType, Collectors.counting()));
        List<CourseStatisticsVo.TypeCountVo> typeStats = typeMap.entrySet().stream()
                .map(entry -> {
                    CourseStatisticsVo.TypeCountVo tv = new CourseStatisticsVo.TypeCountVo();
                    tv.setType(entry.getKey());
                    tv.setTypeName(entry.getKey());
                    tv.setCount(entry.getValue());
                    return tv;
                }).collect(Collectors.toList());
        vo.setTypeStatistics(typeStats);

        // 7. 近7天购买趋势
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<CourseStatisticsVo.DailyCountVo> dailyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date dayStart = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date dayEnd = cal.getTime();

            LambdaQueryWrapper<CourseOrder> dayQuery = new LambdaQueryWrapper<>();
            dayQuery.eq(CourseOrder::getStatus, 1)
                    .ge(CourseOrder::getCreateTime, dayStart)
                    .lt(CourseOrder::getCreateTime, dayEnd);
            List<CourseOrder> dayOrders = courseOrderMapper.selectList(dayQuery);

            CourseStatisticsVo.DailyCountVo dcv = new CourseStatisticsVo.DailyCountVo();
            dcv.setDate(sdf.format(dayStart));
            dcv.setCount((long) dayOrders.size());
            dcv.setRevenue(dayOrders.stream()
                    .map(CourseOrder::getPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            dailyTrend.add(dcv);
        }
        vo.setDailyOrderTrend(dailyTrend);

        // 8. 热门课程TOP10
        Map<Long, List<CourseOrder>> courseOrderMap = allPaidOrders.stream()
                .filter(o -> o.getCourseId() != null)
                .collect(Collectors.groupingBy(CourseOrder::getCourseId));
        Map<Long, String> courseNameMap = allCourses.stream()
                .collect(Collectors.toMap(Course::getId, Course::getName, (a, b) -> a));
        List<CourseStatisticsVo.CourseRankVo> hotCourses = courseOrderMap.entrySet().stream()
                .map(entry -> {
                    CourseStatisticsVo.CourseRankVo crv = new CourseStatisticsVo.CourseRankVo();
                    crv.setCourseId(entry.getKey());
                    crv.setCourseName(courseNameMap.getOrDefault(entry.getKey(), "未知课程"));
                    crv.setOrderCount((long) entry.getValue().size());
                    crv.setTotalRevenue(entry.getValue().stream()
                            .map(CourseOrder::getPrice)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return crv;
                })
                .sorted((a, b) -> Long.compare(b.getOrderCount(), a.getOrderCount()))
                .limit(10)
                .collect(Collectors.toList());
        vo.setHotCourseRank(hotCourses);

        // 9. 热门教练TOP10
        Map<Long, List<CourseOrder>> coachOrderMap = allPaidOrders.stream()
                .filter(o -> o.getCoachId() != null)
                .collect(Collectors.groupingBy(CourseOrder::getCoachId));
        List<Coach> allCoaches = coachMapper.selectList(null);
        Map<Long, String> coachNameMap = allCoaches.stream()
                .collect(Collectors.toMap(Coach::getId, Coach::getName, (a, b) -> a));
        List<CourseStatisticsVo.CoachRankVo> hotCoaches = coachOrderMap.entrySet().stream()
                .map(entry -> {
                    CourseStatisticsVo.CoachRankVo crv = new CourseStatisticsVo.CoachRankVo();
                    crv.setCoachId(entry.getKey());
                    crv.setCoachName(coachNameMap.getOrDefault(entry.getKey(), "未知教练"));
                    crv.setOrderCount((long) entry.getValue().size());
                    crv.setTotalRevenue(entry.getValue().stream()
                            .map(CourseOrder::getPrice)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return crv;
                })
                .sorted((a, b) -> Long.compare(b.getOrderCount(), a.getOrderCount()))
                .limit(10)
                .collect(Collectors.toList());
        vo.setHotCoachRank(hotCoaches);

        return vo;
    }

    /**
     * 获取课程查询条件
     *
     * @param courseQueryPageRequest 查询条件
     * @return 查询条件
     */
    private LambdaQueryWrapper<Course> getCourseQueryWrapper(CourseQueryPageRequest courseQueryPageRequest) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        if (courseQueryPageRequest == null) {
            return queryWrapper;
        }

        String name = courseQueryPageRequest.getName();
        Long coachId = courseQueryPageRequest.getCoachId();
        String difficulty = courseQueryPageRequest.getDifficulty();
        String type = courseQueryPageRequest.getType();
        // 模糊查询课程名称
        queryWrapper.like(StringUtils.isNotBlank(name), Course::getName, name);
        // 精确查询教练ID
        queryWrapper.eq(coachId != null, Course::getCoachId, coachId);
        // 精确查询难度等级
        queryWrapper.eq(StringUtils.isNotBlank(difficulty), Course::getDifficulty, difficulty);
        // 价格范围查询
        queryWrapper.ge(courseQueryPageRequest.getMinPrice() != null,
                Course::getPrice, courseQueryPageRequest.getMinPrice());
        queryWrapper.le(courseQueryPageRequest.getMaxPrice() != null,
                Course::getPrice, courseQueryPageRequest.getMaxPrice());
        // 时长范围查询
        queryWrapper.ge(courseQueryPageRequest.getMinDuration() != null,
                Course::getDuration, courseQueryPageRequest.getMinDuration());
        queryWrapper.le(courseQueryPageRequest.getMaxDuration() != null,
                Course::getDuration, courseQueryPageRequest.getMaxDuration());

        queryWrapper.eq(courseQueryPageRequest.getType() != null, Course::getType, type );
        return queryWrapper;
    }

    @Override
    public String updateCourseImage(Long courseId, MultipartFile file) {
        validateImageFile(file);
        File compressedImage = null;
        try {
            String uploadFilePath = "course/" + StrUtil.format("{}/{}.{}",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                    courseId, "webp");

            compressedImage = compressImage(file);
            String result = cosManager.uploadFile(uploadFilePath, compressedImage);

            // 更新数据库中的图片URL
            Course course = this.getById(courseId);
            if (course == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
            }
            course.setImage(result);
            boolean update = this.updateById(course);
            if (!update) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新课程图片失败");
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
}

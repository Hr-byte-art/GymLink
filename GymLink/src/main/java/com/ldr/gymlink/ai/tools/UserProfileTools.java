package com.ldr.gymlink.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ldr.gymlink.model.entity.Admin;
import com.ldr.gymlink.model.entity.Coach;
import com.ldr.gymlink.model.entity.CoachAppointment;
import com.ldr.gymlink.model.entity.Course;
import com.ldr.gymlink.model.entity.CourseBooking;
import com.ldr.gymlink.model.entity.CourseOrder;
import com.ldr.gymlink.model.entity.EquipmentReservation;
import com.ldr.gymlink.model.entity.Student;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.enums.UserRoleEnum;
import com.ldr.gymlink.model.vo.CoachVo;
import com.ldr.gymlink.model.vo.StudentVo;
import com.ldr.gymlink.service.AdminService;
import com.ldr.gymlink.service.CoachAppointmentService;
import com.ldr.gymlink.service.CoachService;
import com.ldr.gymlink.service.CourseBookingService;
import com.ldr.gymlink.service.CourseOrderService;
import com.ldr.gymlink.service.CourseService;
import com.ldr.gymlink.service.EquipmentReservationService;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.service.UserService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 当前用户画像 AI 工具
 */
@Component
@Slf4j
public class UserProfileTools {

    private static final int DEFAULT_TOP_N = 5;
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");

    @Resource
    private UserService userService;

    @Resource
    private StudentService studentService;

    @Resource
    private CoachService coachService;

    @Resource
    private AdminService adminService;

    @Resource
    private CourseService courseService;

    @Resource
    private CourseOrderService courseOrderService;

    @Resource
    private CourseBookingService courseBookingService;

    @Resource
    private CoachAppointmentService coachAppointmentService;

    @Resource
    private EquipmentReservationService equipmentReservationService;

    @Tool("获取当前登录用户自己的画像信息。参数必须传当前会话用户自己的 userId。会根据角色区分返回学生、教练或管理员信息；如果是学生，还会结合身高体重、已购课程、课程报名、教练预约、器材预约等历史数据实时计算课程推荐分数，生成 Top-N 推荐课程和推荐理由。适用于回答“推荐课程”“帮我制定训练计划”“我适合什么课程”等个性化问题前先调用。")
    public String getCurrentUserProfile(
            @P("当前登录用户的 userId，只能传当前会话用户自己的 id") Long userId) {
        log.info("AI工具调用: getCurrentUserProfile, userId={}", userId);

        if (userId == null) {
            return "未提供有效的 userId，无法获取当前用户画像。";
        }

        User user = userService.getById(userId);
        if (user == null) {
            return "未找到该 userId 对应的用户信息。";
        }

        UserRoleEnum roleEnum = UserRoleEnum.getEnumByValue(user.getRole());
        if (roleEnum == null) {
            return "当前用户角色未知，无法构建用户画像。";
        }

        return switch (roleEnum) {
            case STUDENT -> buildStudentProfile(user);
            case COACH -> buildCoachProfile(user);
            case ADMIN -> buildAdminProfile(user);
        };
    }

    private String buildStudentProfile(User user) {
        StudentVo student = studentService.getStudentByUserId(user.getId());
        if (student == null || student.getId() == null) {
            return "当前用户是学员角色，但未查询到对应的学员档案。";
        }

        List<Course> allCourses = courseService.list();
        Map<Long, Course> courseMap = allCourses.stream()
                .filter(course -> course.getId() != null)
                .collect(Collectors.toMap(Course::getId, Function.identity(), (left, right) -> left));

        List<CourseOrder> allPaidOrders = courseOrderService.list(
                new LambdaQueryWrapper<CourseOrder>()
                        .eq(CourseOrder::getStatus, 1)
                        .orderByDesc(CourseOrder::getCreateTime));

        List<CourseOrder> userOrders = courseOrderService.list(
                new LambdaQueryWrapper<CourseOrder>()
                        .eq(CourseOrder::getStudentId, student.getId())
                        .orderByDesc(CourseOrder::getCreateTime));

        List<CourseBooking> userBookings = courseBookingService.list(
                new LambdaQueryWrapper<CourseBooking>()
                        .eq(CourseBooking::getStudentId, student.getId())
                        .orderByDesc(CourseBooking::getCreateTime));

        List<CoachAppointment> userCoachAppointments = coachAppointmentService.list(
                new LambdaQueryWrapper<CoachAppointment>()
                        .eq(CoachAppointment::getStudentId, student.getId())
                        .orderByDesc(CoachAppointment::getCreateTime));

        List<EquipmentReservation> userEquipmentReservations = equipmentReservationService.list(
                new LambdaQueryWrapper<EquipmentReservation>()
                        .eq(EquipmentReservation::getStudentId, student.getId())
                        .orderByDesc(EquipmentReservation::getCreateTime));

        Set<Long> activeCourseIds = userOrders.stream()
                .filter(order -> Integer.valueOf(1).equals(order.getStatus()))
                .filter(order -> order.getRemainingSessions() != null && order.getRemainingSessions() > 0)
                .map(CourseOrder::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Double> typePreferenceMap = new HashMap<>();
        Map<String, Double> difficultyPreferenceMap = new HashMap<>();
        Map<Long, Double> coachPreferenceMap = new HashMap<>();

        for (CourseOrder order : userOrders) {
            Course course = courseMap.get(order.getCourseId());
            if (course == null) {
                continue;
            }
            increaseWeight(typePreferenceMap, course.getType(), 1.8D);
            increaseWeight(difficultyPreferenceMap, course.getDifficulty(), 1.2D);
            increaseWeight(coachPreferenceMap, course.getCoachId(), 1.0D);
        }

        for (CourseBooking booking : userBookings) {
            Course course = courseMap.get(booking.getCourseId());
            if (course == null) {
                continue;
            }
            increaseWeight(typePreferenceMap, course.getType(), 1.0D);
            increaseWeight(difficultyPreferenceMap, course.getDifficulty(), 0.8D);
            increaseWeight(coachPreferenceMap, course.getCoachId(), 0.6D);
        }

        for (CoachAppointment appointment : userCoachAppointments) {
            increaseWeight(coachPreferenceMap, appointment.getCoachId(), 0.8D);
        }

        Map<Long, Long> popularityMap = allPaidOrders.stream()
                .map(CourseOrder::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Double bmi = calculateBmi(student.getHeight(), student.getWeight());
        String inferredGoal = inferGoalByBmi(bmi);
        Set<String> bmiPreferredTypes = inferPreferredCourseTypesByBmi(bmi);
        boolean newUser = userOrders.isEmpty() && userBookings.isEmpty() && userCoachAppointments.isEmpty();

        List<ScoredCourse> recommendations = allCourses.stream()
                .filter(course -> course.getId() != null)
                .filter(course -> !activeCourseIds.contains(course.getId()))
                .map(course -> scoreCourse(course, student, popularityMap, typePreferenceMap,
                        difficultyPreferenceMap, coachPreferenceMap, bmiPreferredTypes, newUser))
                .sorted(Comparator.comparingDouble(ScoredCourse::score).reversed())
                .limit(DEFAULT_TOP_N)
                .toList();

        String recentOrders = summarizeRecentOrders(userOrders, courseMap);
        String recentBookings = summarizeRecentBookings(userBookings, courseMap);
        String preferredTypes = summarizeTopPreferences(typePreferenceMap, this::getCourseTypeName);
        String preferredDifficulties = summarizeTopPreferences(difficultyPreferenceMap, this::getDifficultyName);

        StringBuilder sb = new StringBuilder();
        sb.append("当前登录用户画像（仅当前用户本人）\n");
        sb.append("- userId: ").append(user.getId()).append('\n');
        sb.append("- role: student\n");
        sb.append("- username: ").append(defaultText(student.getUsername(), user.getUsername())).append('\n');
        sb.append("- 姓名: ").append(defaultText(student.getName(), "未填写")).append('\n');
        sb.append("- 性别: ").append(getGenderName(student.getGender())).append('\n');
        sb.append("- 身高: ").append(formatDecimal(student.getHeight())).append(" cm\n");
        sb.append("- 体重: ").append(formatDecimal(student.getWeight())).append(" kg\n");
        sb.append("- BMI: ").append(bmi == null ? "暂无可计算数据" : DECIMAL_FORMAT.format(bmi)).append('\n');
        sb.append("- 推断训练目标: ").append(inferredGoal).append('\n');
        sb.append("- 账户余额: ").append(formatCurrency(student.getBalance())).append('\n');
        sb.append("- 已购课程订单数: ").append(userOrders.size()).append('\n');
        sb.append("- 课程报名次数: ").append(userBookings.size()).append('\n');
        sb.append("- 教练预约次数: ").append(userCoachAppointments.size()).append('\n');
        sb.append("- 器材预约次数: ").append(userEquipmentReservations.size()).append('\n');
        sb.append("- 偏好课程类型: ").append(preferredTypes).append('\n');
        sb.append("- 偏好课程难度: ").append(preferredDifficulties).append('\n');
        sb.append("- 最近购课记录: ").append(recentOrders).append('\n');
        sb.append("- 最近报名记录: ").append(recentBookings).append('\n');
        sb.append('\n');
        sb.append("个性化课程推荐 Top-").append(recommendations.size()).append("（后端实时评分）\n");

        if (recommendations.isEmpty()) {
            sb.append("- 暂无可推荐课程，可结合用户目标改用通用健身建议。\n");
        } else {
            for (int i = 0; i < recommendations.size(); i++) {
                ScoredCourse recommendation = recommendations.get(i);
                Course course = recommendation.course();
                sb.append(i + 1).append(". ");
                sb.append(defaultText(course.getName(), "未命名课程"));
                sb.append(" | 推荐分=").append(DECIMAL_FORMAT.format(recommendation.score()));
                sb.append(" | 类型=").append(getCourseTypeName(course.getType()));
                sb.append(" | 难度=").append(getDifficultyName(course.getDifficulty()));
                sb.append(" | 价格=").append(formatCurrency(course.getPrice()));
                sb.append(" | 时长=").append(course.getDuration() == null ? "未知" : course.getDuration() + " 分钟");
                if (course.getCoachId() != null) {
                    Coach coach = coachService.getById(course.getCoachId());
                    if (coach != null) {
                        sb.append(" | 教练=").append(defaultText(coach.getName(), "未知"));
                    }
                }
                sb.append('\n');
                sb.append("   推荐理由: ").append(String.join("；", recommendation.reasons())).append('\n');
            }
        }

        sb.append('\n');
        sb.append("给 AI 的使用建议\n");
        sb.append("- 以上内容只代表当前 userId 对应用户自己的画像，不能扩展到其他用户。\n");
        sb.append("- 如果用户询问推荐课程、训练计划、减脂/增肌建议，应优先结合 BMI、历史偏好、最近购买和报名记录来回答。\n");
        sb.append("- 如果用户没有明确目标，可先根据“推断训练目标”和 Top-N 推荐课程给出分层建议。\n");
        return sb.toString();
    }

    private String buildCoachProfile(User user) {
        CoachVo coach = coachService.getCoachByUserId(user.getId());
        if (coach == null || coach.getId() == null) {
            return "当前用户是教练角色，但未查询到对应的教练档案。";
        }

        List<Course> coachCourses = courseService.list(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getCoachId, coach.getId())
                        .orderByDesc(Course::getCreateTime));

        List<CoachAppointment> appointments = coachAppointmentService.list(
                new LambdaQueryWrapper<CoachAppointment>()
                        .eq(CoachAppointment::getCoachId, coach.getId())
                        .orderByDesc(CoachAppointment::getCreateTime));

        long pendingCount = appointments.stream().filter(item -> Integer.valueOf(0).equals(item.getStatus())).count();
        long confirmedCount = appointments.stream().filter(item -> Integer.valueOf(1).equals(item.getStatus())).count();

        StringBuilder sb = new StringBuilder();
        sb.append("当前登录用户画像（仅当前用户本人）\n");
        sb.append("- userId: ").append(user.getId()).append('\n');
        sb.append("- role: coach\n");
        sb.append("- username: ").append(defaultText(coach.getUsername(), user.getUsername())).append('\n');
        sb.append("- 姓名: ").append(defaultText(coach.getName(), "未填写")).append('\n');
        sb.append("- 性别: ").append(getGenderName(coach.getGender())).append('\n');
        sb.append("- 年龄: ").append(coach.getAge() == null ? "未填写" : coach.getAge()).append('\n');
        sb.append("- 电话: ").append(defaultText(coach.getPhone(), "未填写")).append('\n');
        sb.append("- 专长方向: ").append(getCoachSpecialtyName(coach.getSpecialty())).append('\n');
        sb.append("- 简介: ").append(defaultText(coach.getIntro(), "暂无")).append('\n');
        sb.append("- 私教价格: ").append(formatCurrency(coach.getPrice())).append('\n');
        sb.append("- 发布课程数: ").append(coachCourses.size()).append('\n');
        sb.append("- 教练预约总数: ").append(appointments.size()).append('\n');
        sb.append("- 待确认预约数: ").append(pendingCount).append('\n');
        sb.append("- 已确认预约数: ").append(confirmedCount).append('\n');
        sb.append("- 最近课程: ").append(coachCourses.stream()
                .limit(3)
                .map(course -> defaultText(course.getName(), "未命名课程"))
                .collect(Collectors.joining("、", "", coachCourses.isEmpty() ? "暂无" : ""))).append('\n');
        sb.append('\n');
        sb.append("给 AI 的使用建议\n");
        sb.append("- 这是教练用户自己的画像，可用于回答其课程运营、个人品牌介绍、训练风格说明等问题。\n");
        sb.append("- 教练角色不生成学员购课推荐，除非用户明确以学员视角咨询通用课程建议。\n");
        return sb.toString();
    }

    private String buildAdminProfile(User user) {
        Admin admin = user.getAssociatedUserId() == null ? null : adminService.getById(user.getAssociatedUserId());

        StringBuilder sb = new StringBuilder();
        sb.append("当前登录用户画像（仅当前用户本人）\n");
        sb.append("- userId: ").append(user.getId()).append('\n');
        sb.append("- role: admin\n");
        sb.append("- username: ").append(defaultText(user.getUsername(), "未填写")).append('\n');
        sb.append("- 姓名: ").append(admin == null ? "未填写" : defaultText(admin.getName(), "未填写")).append('\n');
        sb.append("- 电话: ").append(admin == null ? "未填写" : defaultText(admin.getPhone(), "未填写")).append('\n');
        sb.append("- 创建时间: ").append(formatDateTime(user.getCreateTime())).append('\n');
        sb.append('\n');
        sb.append("给 AI 的使用建议\n");
        sb.append("- 这是管理员用户自己的基础信息，适合用于系统管理场景说明。\n");
        sb.append("- 管理员角色默认不生成学员侧个性化课程推荐。\n");
        return sb.toString();
    }

    private ScoredCourse scoreCourse(Course course,
                                     StudentVo student,
                                     Map<Long, Long> popularityMap,
                                     Map<String, Double> typePreferenceMap,
                                     Map<String, Double> difficultyPreferenceMap,
                                     Map<Long, Double> coachPreferenceMap,
                                     Set<String> bmiPreferredTypes,
                                     boolean newUser) {
        double score = 20D;
        List<String> reasons = new ArrayList<>();

        Long popularity = popularityMap.getOrDefault(course.getId(), 0L);
        if (popularity > 0) {
            double popularityScore = Math.min(popularity * 2D, 16D);
            score += popularityScore;
            reasons.add("平台内热度较高");
        }

        Double typeWeight = typePreferenceMap.get(course.getType());
        if (typeWeight != null && typeWeight > 0) {
            score += Math.min(typeWeight * 12D, 28D);
            reasons.add("与用户历史购课/报名偏好高度匹配");
        }

        Double difficultyWeight = difficultyPreferenceMap.get(course.getDifficulty());
        if (difficultyWeight != null && difficultyWeight > 0) {
            score += Math.min(difficultyWeight * 6D, 10D);
            reasons.add("难度与用户历史选择接近");
        }

        Double coachWeight = coachPreferenceMap.get(course.getCoachId());
        if (coachWeight != null && coachWeight > 0) {
            score += Math.min(coachWeight * 4D, 8D);
            reasons.add("与用户过往偏好的教练路线接近");
        }

        if (bmiPreferredTypes.contains(course.getType())) {
            score += 12D;
            reasons.add("符合用户当前体型阶段的训练方向");
        }

        if (newUser && isBeginnerFriendly(course)) {
            score += 10D;
            reasons.add("对新用户更友好，入门成本较低");
        }

        if (student.getBalance() != null && course.getPrice() != null) {
            if (student.getBalance().compareTo(course.getPrice()) >= 0) {
                score += 5D;
                reasons.add("当前账户余额可覆盖课程价格");
            } else {
                score -= 3D;
                reasons.add("课程价格略高于当前余额");
            }
        }

        if (course.getDuration() != null && course.getDuration() >= 45 && course.getDuration() <= 90) {
            score += 3D;
            reasons.add("课程时长适中，便于稳定执行");
        }

        if (reasons.isEmpty()) {
            reasons.add("综合课程基础属性与平台热度推荐");
        }

        return new ScoredCourse(course, score, reasons);
    }

    private boolean isBeginnerFriendly(Course course) {
        if (course == null) {
            return false;
        }
        String difficulty = course.getDifficulty();
        return difficulty == null
                || "beginner".equalsIgnoreCase(difficulty)
                || "初级".equals(difficulty);
    }

    private Double calculateBmi(BigDecimal height, BigDecimal weight) {
        if (height == null || weight == null || BigDecimal.ZERO.compareTo(height) >= 0) {
            return null;
        }
        BigDecimal heightMeter = height.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        if (BigDecimal.ZERO.compareTo(heightMeter) >= 0) {
            return null;
        }
        BigDecimal bmi = weight.divide(heightMeter.multiply(heightMeter), 2, RoundingMode.HALF_UP);
        return bmi.doubleValue();
    }

    private String inferGoalByBmi(Double bmi) {
        if (bmi == null) {
            return "暂无足够身体数据，建议先补充身高和体重";
        }
        if (bmi < 18.5D) {
            return "偏向增肌与基础力量提升";
        }
        if (bmi < 24D) {
            return "偏向综合塑形与体能维持";
        }
        if (bmi < 28D) {
            return "偏向减脂塑形与心肺提升";
        }
        return "偏向减脂、恢复体能与循序渐进训练";
    }

    private Set<String> inferPreferredCourseTypesByBmi(Double bmi) {
        if (bmi == null) {
            return Set.of("3", "5");
        }
        if (bmi < 18.5D) {
            return Set.of("1", "3", "4");
        }
        if (bmi < 24D) {
            return Set.of("3", "4", "5", "6");
        }
        if (bmi < 28D) {
            return Set.of("2", "3", "5", "7");
        }
        return Set.of("2", "3", "5", "7");
    }

    private String summarizeRecentOrders(List<CourseOrder> orders, Map<Long, Course> courseMap) {
        if (orders.isEmpty()) {
            return "暂无";
        }
        return orders.stream()
                .limit(3)
                .map(order -> {
                    Course course = courseMap.get(order.getCourseId());
                    String courseName = course == null ? "未知课程" : defaultText(course.getName(), "未知课程");
                    return courseName + "(" + formatDateTime(order.getCreateTime()) + ")";
                })
                .collect(Collectors.joining("、"));
    }

    private String summarizeRecentBookings(List<CourseBooking> bookings, Map<Long, Course> courseMap) {
        if (bookings.isEmpty()) {
            return "暂无";
        }
        return bookings.stream()
                .limit(3)
                .map(booking -> {
                    Course course = courseMap.get(booking.getCourseId());
                    String courseName = course == null ? "未知课程" : defaultText(course.getName(), "未知课程");
                    return courseName + "(" + formatDateTime(booking.getCreateTime()) + ")";
                })
                .collect(Collectors.joining("、"));
    }

    private String summarizeTopPreferences(Map<String, Double> preferenceMap, Function<String, String> labelMapper) {
        if (preferenceMap.isEmpty()) {
            return "暂无明显偏好";
        }
        return preferenceMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(entry -> labelMapper.apply(entry.getKey()))
                .collect(Collectors.joining("、"));
    }

    private <T> void increaseWeight(Map<T, Double> targetMap, T key, double weight) {
        if (key == null) {
            return;
        }
        targetMap.merge(key, weight, Double::sum);
    }

    private String getCourseTypeName(String type) {
        if (type == null) {
            return "未分类";
        }
        return switch (type) {
            case "1" -> "私教课程";
            case "2" -> "团体训练课程";
            case "3" -> "功能性训练课程";
            case "4" -> "力量训练课程";
            case "5" -> "瑜伽课程";
            case "6" -> "普拉提课程";
            case "7" -> "康复/矫正训练课程";
            case "8" -> "专项运动表现课程";
            case "9" -> "孕产/产后修复课程";
            case "10" -> "老年/青少年体适能课程";
            case "11" -> "线上课程";
            default -> type;
        };
    }

    private String getDifficultyName(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            return "未设置";
        }
        return switch (difficulty.toLowerCase()) {
            case "beginner" -> "初级";
            case "intermediate" -> "中级";
            case "advanced" -> "高级";
            default -> difficulty;
        };
    }

    private String getCoachSpecialtyName(String specialty) {
        if (specialty == null || specialty.isBlank()) {
            return "未填写";
        }
        return switch (specialty) {
            case "1" -> "私人健身教练";
            case "2" -> "团体课教练";
            case "3" -> "力量训练";
            case "4" -> "瑜伽";
            case "5" -> "有氧运动";
            case "6" -> "康复/矫正训练教练";
            case "7" -> "营养与生活方式教练";
            case "8" -> "专项运动教练";
            case "9" -> "线上健身教练";
            default -> specialty;
        };
    }

    private String getGenderName(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            case 3 -> "未知";
            default -> "未知";
        };
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "未填写" : value.stripTrailingZeros().toPlainString();
    }

    private String formatCurrency(BigDecimal value) {
        return value == null ? "未填写" : value.stripTrailingZeros().toPlainString() + " 元";
    }

    private String formatDateTime(Date date) {
        return date == null ? "未知时间" : DATE_TIME_FORMAT.format(date);
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private record ScoredCourse(Course course, double score, List<String> reasons) {
    }
}

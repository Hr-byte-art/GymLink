package com.ldr.gymlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.model.dto.student.AddStudentRequest;
import com.ldr.gymlink.model.dto.student.StudentQueryPageRequest;
import com.ldr.gymlink.model.dto.student.UpdateStudentRequest;
import com.ldr.gymlink.model.entity.Student;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldr.gymlink.model.vo.StudentVo;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * @author 木子宸
 * @description 针对表【student(学员/用户信息表)】的数据库操作Service
 * @createDate 2025-11-29 20:39:16
 */
public interface StudentService extends IService<Student> {

    /**
     * 根据用户id获取学员信息
     *
     * @param userId 用户id
     * @return 学员信息
     */
    StudentVo getStudentByUserId(Long userId);

    /**
     * 添加学员
     *
     * @param addStudentRequest 添加学员请求
     * @return 学员信息
     */
    StudentVo addStudent(AddStudentRequest addStudentRequest);

    /**
     * 获取所有学员信息(分页)
     *
     * @return 学员信息列表
     */
    Page<StudentVo> listStudentPage(StudentQueryPageRequest studentQueryPageRequest);

    /**
     * 删除学员
     *
     * @param id 学员id
     * @return 是否删除成功
     */
    boolean deleteStudent(Long id);

    /**
     * 修改学员信息
     *
     * @param updateStudentRequest 修改学员信息请求
     * @return 是否修改成功
     */
    boolean updateStudent(Long id,UpdateStudentRequest updateStudentRequest);

    /**
     * 根据id获取学员信息
     *
     * @param id 学员id
     * @return 学员信息
     */
    StudentVo getStudentById(Long id);

    /**
     * 学员充值
     *
     * @param id      学员id
     * @param money   充值金额
     * @return 是否充值成功
     */
    boolean studentTopUp(Long id, BigDecimal money);

    /**
     * 学员购买课程
     *
     * @param studentId 学员id
     * @param courseId  课程id
     * @return 是否购买成功
     */
    boolean studentsPurchaseCourses(Long studentId, Long courseId);

    /**
     * 修改用户头像
     *
     * @param studentId 学员id
     * @param avatar    头像文件
     * @return 是否修改成功
     */
    String updateStudentAvatar(Long studentId, MultipartFile avatar);

    /**
     * 获取学员已购课程列表(分页)
     *
     * @param request 查询请求
     * @return 已购课程列表
     */
    Page<com.ldr.gymlink.model.vo.PurchasedCourseVo> getPurchasedCourses(com.ldr.gymlink.model.dto.student.PurchasedCourseQueryRequest request);

    /**
     * 获取学员已购课程ID列表
     *
     * @param studentId 学员ID
     * @return 已购课程ID列表
     */
    java.util.List<Long> getPurchasedCourseIds(Long studentId);

    /**
     * 申请课程退款
     *
     * @param orderId 订单ID
     * @return 是否申请成功
     */
    boolean refundCourse(Long orderId);

    /**
     * 管理员审核通过退款
     *
     * @param orderId 订单ID
     * @return 是否操作成功
     */
    boolean approveRefund(Long orderId);

    /**
     * 管理员拒绝退款
     *
     * @param orderId 订单ID
     * @return 是否操作成功
     */
    boolean rejectRefund(Long orderId);

    /**
     * 获取退款订单列表（管理员用）
     *
     * @param request 查询请求
     * @return 退款订单列表
     */
    Page<com.ldr.gymlink.model.vo.PurchasedCourseVo> getRefundOrders(com.ldr.gymlink.model.dto.student.PurchasedCourseQueryRequest request);
}

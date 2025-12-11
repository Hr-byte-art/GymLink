package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.model.dto.student.AddStudentRequest;
import com.ldr.gymlink.model.dto.student.StudentQueryPageRequest;
import com.ldr.gymlink.model.dto.student.UpdateStudentRequest;
import com.ldr.gymlink.model.vo.StudentVo;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.utils.ResultUtils;
import com.ldr.gymlink.utils.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;

/**
 * @Author 王哈哈
 * @Date 2025/11/30 01:03:41
 * @Description
 */
@RestController
@RequestMapping("/student")
@Tag(name = "学员管理")
public class StudentController {

    @Resource
    private StudentService studentService;

    @PostMapping("/addStudent")
    @Operation(summary = "添加学员")
    public BaseResponse<StudentVo> addCoach(@RequestBody AddStudentRequest addStudentRequest) {
        StudentVo studentVo = studentService.addStudent(addStudentRequest);
        return ResultUtils.success(studentVo);
    }

    @GetMapping("/getStudentByUserId")
    @Operation(summary = "通过用户id获取学员信息")
    public BaseResponse<StudentVo> getStudentByUserId(@RequestParam Long userId) {
        StudentVo studentVo = studentService.getStudentByUserId(userId);
        return ResultUtils.success(studentVo);
    }

    @PostMapping("/ListStudent")
    @Operation(summary = "分页查询学员信息")
    public BaseResponse<Page<StudentVo>> listStudent(
            @RequestBody StudentQueryPageRequest studentQueryPageRequest) {
        Page<StudentVo> studentVos = studentService.listStudentPage(studentQueryPageRequest);
        return ResultUtils.success(studentVos);
    }

    @PostMapping("/deleteStudent")
    @Operation(summary = "删除学员信息")
    public BaseResponse<Boolean> deleteStudent(@RequestParam Long id) {
        boolean delete = studentService.deleteStudent(id);
        return ResultUtils.success(delete);
    }

    @PostMapping("/updateStudent")
    @Operation(summary = "更新学员信息")
    public BaseResponse<Boolean> updateStudent(
            @RequestParam Long id,
            @RequestBody UpdateStudentRequest updateStudentRequest) {
        boolean update = studentService.updateStudent(id, updateStudentRequest);
        return ResultUtils.success(update);
    }

    @GetMapping("/getStudentById")
    @Operation(summary = "获取学员信息")
    public BaseResponse<StudentVo> getStudentById(@RequestParam Long id) {
        StudentVo studentVo = studentService.getStudentById(id);
        return ResultUtils.success(studentVo);
    }

    @GetMapping("/studentTopUp")
    @Operation(summary = "学员充值")
    public BaseResponse<Boolean> studentTopUp(@RequestParam Long id, @RequestParam BigDecimal money) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "学员id不能为空");
        ThrowUtils.throwIf(money == null, ErrorCode.PARAMS_ERROR, "充值金额不能为空");
        boolean topUp = studentService.studentTopUp(id, money);
        return ResultUtils.success(topUp);
    }

    @GetMapping("/studentsPurchaseCourses")
    @Operation(summary = "学员购买课程")
    public BaseResponse<Boolean> studentsPurchaseCourses(@RequestParam Long studentId, @RequestParam Long courseId) {
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员id不能为空");
        ThrowUtils.throwIf(courseId == null, ErrorCode.PARAMS_ERROR, "课程id不能为空");
        boolean purchaseCourses = studentService.studentsPurchaseCourses(studentId, courseId);
        return ResultUtils.success(purchaseCourses);
    }

    @PostMapping("/updateStudentAvatar")
    @Operation(summary = "修改学员头像")
    public BaseResponse<String> updateStudentAvatar(@RequestParam Long studentId, @RequestParam MultipartFile avatar) {
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "用户id不能为空");
        String updatePath = studentService.updateStudentAvatar(studentId, avatar);
        return ResultUtils.success(updatePath);
    }

    @PostMapping("/getPurchasedCourses")
    @Operation(summary = "获取学员已购课程列表")
    public BaseResponse<Page<com.ldr.gymlink.model.vo.PurchasedCourseVo>> getPurchasedCourses(
            @RequestBody com.ldr.gymlink.model.dto.student.PurchasedCourseQueryRequest request) {
        Page<com.ldr.gymlink.model.vo.PurchasedCourseVo> page = studentService.getPurchasedCourses(request);
        return ResultUtils.success(page);
    }

    @GetMapping("/getPurchasedCourseIds")
    @Operation(summary = "获取学员已购课程ID列表")
    public BaseResponse<java.util.List<Long>> getPurchasedCourseIds(@RequestParam Long studentId) {
        ThrowUtils.throwIf(studentId == null, ErrorCode.PARAMS_ERROR, "学员id不能为空");
        java.util.List<Long> courseIds = studentService.getPurchasedCourseIds(studentId);
        return ResultUtils.success(courseIds);
    }

    @GetMapping("/refundCourse")
    @Operation(summary = "申请课程退款")
    public BaseResponse<Boolean> refundCourse(@RequestParam Long orderId) {
        ThrowUtils.throwIf(orderId == null, ErrorCode.PARAMS_ERROR, "订单ID不能为空");
        boolean result = studentService.refundCourse(orderId);
        return ResultUtils.success(result);
    }

    @GetMapping("/approveRefund")
    @Operation(summary = "管理员审核通过退款")
    public BaseResponse<Boolean> approveRefund(@RequestParam Long orderId) {
        ThrowUtils.throwIf(orderId == null, ErrorCode.PARAMS_ERROR, "订单ID不能为空");
        boolean result = studentService.approveRefund(orderId);
        return ResultUtils.success(result);
    }

    @GetMapping("/rejectRefund")
    @Operation(summary = "管理员拒绝退款")
    public BaseResponse<Boolean> rejectRefund(@RequestParam Long orderId) {
        ThrowUtils.throwIf(orderId == null, ErrorCode.PARAMS_ERROR, "订单ID不能为空");
        boolean result = studentService.rejectRefund(orderId);
        return ResultUtils.success(result);
    }

    @PostMapping("/getRefundOrders")
    @Operation(summary = "获取退款订单列表（管理员）")
    public BaseResponse<Page<com.ldr.gymlink.model.vo.PurchasedCourseVo>> getRefundOrders(
            @RequestBody com.ldr.gymlink.model.dto.student.PurchasedCourseQueryRequest request) {
        Page<com.ldr.gymlink.model.vo.PurchasedCourseVo> page = studentService.getRefundOrders(request);
        return ResultUtils.success(page);
    }
}

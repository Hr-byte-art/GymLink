package com.ldr.gymlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.model.dto.student.AddStudentRequest;
import com.ldr.gymlink.model.dto.student.StudentQueryPageRequest;
import com.ldr.gymlink.model.dto.student.UpdateStudentRequest;
import com.ldr.gymlink.model.vo.StudentVo;
import com.ldr.gymlink.service.StudentService;
import com.ldr.gymlink.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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

}

package com.ldr.gymlink.controller;

import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.model.dto.comment.AddCommentRequest;
import com.ldr.gymlink.model.vo.CommentVo;
import com.ldr.gymlink.service.CommentService;
import com.ldr.gymlink.service.ExperienceReactionService;
import com.ldr.gymlink.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author 王哈哈
 * @Date 2025/12/02 21:09:00
 * @Description 评论管理
 */
@RestController
@RequestMapping("/comment")
@Tag(name = "评论管理")
public class CommentController {

    @Resource
    private CommentService commentService;


    @PostMapping("/addComment")
    @Operation(summary = "发布评论或回复")
    public BaseResponse<CommentVo> addComment(@RequestBody AddCommentRequest addCommentRequest) {
        CommentVo commentVo = commentService.addComment(addCommentRequest);
        return ResultUtils.success(commentVo);
    }

    @PostMapping("/deleteComment")
    @Operation(summary = "删除评论（级联删除所有回复）")
    public BaseResponse<Boolean> deleteComment(@RequestParam Long id) {
        boolean delete = commentService.deleteComment(id);
        return ResultUtils.success(delete);
    }

    @GetMapping("/getCommentTree")
    @Operation(summary = "获取某个经验的所有评论（树形结构）")
    public BaseResponse<List<CommentVo>> getCommentTree(@RequestParam Long experienceId) {
        List<CommentVo> commentTree = commentService.getCommentTree(experienceId);
        return ResultUtils.success(commentTree);
    }

    @GetMapping("/getReplies")
    @Operation(summary = "获取某条评论的所有回复")
    public BaseResponse<List<CommentVo>> getReplies(@RequestParam Long parentId) {
        List<CommentVo> replies = commentService.getReplies(parentId);
        return ResultUtils.success(replies);
    }

    @PostMapping("/likeComment")
    @Operation(summary = "点赞评论")
    public BaseResponse<Boolean> likeComment(@RequestParam Long id) {
        boolean like = commentService.likeComment(id);
        return ResultUtils.success(like);
    }

    @PostMapping("/unlikeComment")
    @Operation(summary = "取消点赞")
    public BaseResponse<Boolean> unlikeComment(@RequestParam Long id) {
        boolean unlike = commentService.unlikeComment(id);
        return ResultUtils.success(unlike);
    }


}

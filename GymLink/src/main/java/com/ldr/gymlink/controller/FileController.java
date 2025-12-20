package com.ldr.gymlink.controller;

import com.ldr.gymlink.common.BaseResponse;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.manager.CosManager;
import com.ldr.gymlink.utils.ResultUtils;
import com.luciad.imageio.webp.WebPWriteParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 * 提供通用的图片上传接口
 */
@RestController
@RequestMapping("/file")
@Tag(name = "文件管理")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    // 允许的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    // 最大文件大小 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 通用图片上传接口
     * 用于新建实体时先上传图片，获取图片URL后再创建实体
     *
     * @param image 图片文件
     * @param type  图片类型（course/equipment/recipe等），用于分类存储
     * @return 图片访问URL
     */
    @PostMapping("/uploadImage")
    @Operation(summary = "上传图片")
    public BaseResponse<String> uploadImage(
            @RequestParam MultipartFile image,
            @RequestParam(defaultValue = "common") String type) {
        
        // 1. 验证文件
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择要上传的图片");
        }

        // 2. 验证文件类型
        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持 jpg、png、webp、gif 格式的图片");
        }

        // 3. 验证文件大小
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小不能超过5MB");
        }

        File compressedImage = null;
        File tempFile = null;
        try {
            // 4. 生成唯一文件名
            String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + ".webp";

            // 5. 构建上传路径
            String uploadFilePath = String.format("gymlink/%s/%s", type, uniqueFileName);

            // 6. 压缩图片为WebP格式
            compressedImage = compressImage(image);

            // 7. 上传到COS
            String imageUrl = cosManager.uploadFile(uploadFilePath, compressedImage);

            if (imageUrl == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败");
            }

            log.info("图片上传成功: type={}, url={}", type, imageUrl);
            return ResultUtils.success(imageUrl);

        } catch (IOException e) {
            log.error("图片处理失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片处理失败: " + e.getMessage());
        } finally {
            // 清理临时文件
            if (compressedImage != null && compressedImage.exists()) {
                compressedImage.delete();
            }
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * 压缩图片为WebP格式
     */
    private File compressImage(MultipartFile multipartFile) throws IOException {
        float quality = 0.5f;
        File oldFile = null;
        File newFile = null;

        try {
            // 获取原始文件扩展名
            String originalFilename = multipartFile.getOriginalFilename();
            String extension = ".tmp";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            oldFile = File.createTempFile("old-", extension);
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

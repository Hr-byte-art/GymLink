package com.ldr.gymlink.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldr.gymlink.ai.aiService.RecipeNutritionAnalyzer;
import com.ldr.gymlink.exception.BusinessException;
import com.ldr.gymlink.exception.ErrorCode;
import com.ldr.gymlink.manager.CosManager;
import com.ldr.gymlink.mapper.RecipeMapper;
import com.ldr.gymlink.model.dto.recipe.AddRecipeRequest;
import com.ldr.gymlink.model.dto.recipe.NutritionAnalysisResult;
import com.ldr.gymlink.model.dto.recipe.RecipeQueryPageRequest;
import com.ldr.gymlink.model.dto.recipe.UpdateRecipeRequest;
import com.ldr.gymlink.model.entity.Recipe;
import com.ldr.gymlink.model.vo.RecipeVo;
import com.ldr.gymlink.service.RecipeService;
import com.luciad.imageio.webp.WebPWriteParam;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author 木子宸
 * @description 针对表【recipe(健身菜谱表)】的数据库操作Service实现
 * @createDate 2025-12-01 21:36:28
 */
@Service
@Slf4j
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe>
        implements RecipeService {

    @Resource
    private CosManager cosManager;

    @Resource
    private QwenChatModel qwenChatModel;

    @Resource
    private ObjectMapper objectMapper;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");

    @Override
    public RecipeVo addRecipe(AddRecipeRequest addRecipeRequest) {
        Recipe recipe = new Recipe();
        BeanUtils.copyProperties(addRecipeRequest, recipe);
        recipe.setCreateTime(new Date());
        boolean save = this.save(recipe);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        
        // 异步调用 AI 分析营养信息
        analyzeAndUpdateNutritionAsync(recipe.getId(), recipe.getTitle(), recipe.getContent());
        
        RecipeVo recipeVo = new RecipeVo();
        BeanUtils.copyProperties(recipe, recipeVo);
        return recipeVo;
    }

    /**
     * 异步分析并更新食谱营养信息
     */
    @Async
    public void analyzeAndUpdateNutritionAsync(Long recipeId, String title, String content) {
        try {
            log.info("开始AI营养分析: recipeId={}", recipeId);
            NutritionAnalysisResult result = analyzeNutrition(title, content);
            if (result != null) {
                Recipe recipe = new Recipe();
                recipe.setId(recipeId);
                recipe.setCalories(result.getCalories());
                recipe.setProtein(result.getProtein());
                recipe.setCarbs(result.getCarbs());
                recipe.setFat(result.getFat());
                recipe.setPrepTime(result.getPrepTime());
                recipe.setCookTime(result.getCookTime());
                recipe.setServings(result.getServings());
                recipe.setDifficulty(result.getDifficulty());
                this.updateById(recipe);
                log.info("AI营养分析完成: recipeId={}, calories={}, protein={}", 
                        recipeId, result.getCalories(), result.getProtein());
            }
        } catch (Exception e) {
            log.error("AI营养分析失败: recipeId={}", recipeId, e);
        }
    }

    /**
     * 调用 AI 分析食谱营养信息
     */
    private NutritionAnalysisResult analyzeNutrition(String title, String content) {
        try {
            RecipeNutritionAnalyzer analyzer = AiServices.builder(RecipeNutritionAnalyzer.class)
                    .chatModel(qwenChatModel)
                    .build();
            
            String recipeInfo = String.format("食谱标题：%s\n食谱内容：%s", title, content);
            String jsonResult = analyzer.analyzeNutrition(recipeInfo);
            
            log.debug("AI营养分析原始结果: {}", jsonResult);
            
            // 清理可能的 markdown 代码块标记
            jsonResult = cleanJsonResponse(jsonResult);
            
            return objectMapper.readValue(jsonResult, NutritionAnalysisResult.class);
        } catch (Exception e) {
            log.error("解析AI营养分析结果失败", e);
            return getDefaultNutritionResult();
        }
    }

    /**
     * 清理 AI 返回的 JSON（去除可能的 markdown 代码块）
     */
    private String cleanJsonResponse(String response) {
        if (response == null) return "{}";
        response = response.trim();
        // 去除 ```json 和 ``` 标记
        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        return response.trim();
    }

    /**
     * 获取默认营养分析结果（当 AI 分析失败时使用）
     */
    private NutritionAnalysisResult getDefaultNutritionResult() {
        NutritionAnalysisResult result = new NutritionAnalysisResult();
        result.setCalories(300);
        result.setProtein(20);
        result.setCarbs(30);
        result.setFat(10);
        result.setPrepTime(15);
        result.setCookTime(20);
        result.setServings(1);
        result.setDifficulty("medium");
        return result;
    }

    @Override
    public boolean deleteRecipe(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return this.removeById(id);
    }

    @Override
    public boolean updateRecipe(Long id, UpdateRecipeRequest updateRecipeRequest) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Recipe recipe = new Recipe();
        BeanUtils.copyProperties(updateRecipeRequest, recipe);
        recipe.setId(id);
        return this.updateById(recipe);
    }

    @Override
    public RecipeVo getRecipeById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Recipe recipe = this.getById(id);
        if (recipe == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        RecipeVo recipeVo = new RecipeVo();
        BeanUtils.copyProperties(recipe, recipeVo);
        return recipeVo;
    }

    @Override
    public Page<RecipeVo> listRecipePage(RecipeQueryPageRequest recipeQueryPageRequest) {
        long current = recipeQueryPageRequest.getPageNum();
        long pageSize = recipeQueryPageRequest.getPageSize();
        Page<Recipe> page = new Page<>(current, pageSize);
        QueryWrapper<Recipe> queryWrapper = new QueryWrapper<>();
        String title = recipeQueryPageRequest.getTitle();
        String tags = recipeQueryPageRequest.getTags();
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        if (StringUtils.isNotBlank(tags)) {
            queryWrapper.like("tags", tags);
        }
        Page<Recipe> recipePage = this.page(page, queryWrapper);
        Page<RecipeVo> recipeVoPage = new Page<>(current, pageSize, recipePage.getTotal());
        List<Recipe> records = recipePage.getRecords();
        List<RecipeVo> recipeVoList = new ArrayList<>();
        for (Recipe recipe : records) {
            RecipeVo recipeVo = new RecipeVo();
            BeanUtils.copyProperties(recipe, recipeVo);
            recipeVoList.add(recipeVo);
        }
        recipeVoPage.setRecords(recipeVoList);
        return recipeVoPage;
    }

    @Override
    public String updateRecipeImage(Long recipeId, MultipartFile file) {
        validateImageFile(file);
        File compressedImage = null;
        try {
            String uploadFilePath = "recipe/" + StrUtil.format("{}/{}.{}", 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")), 
                    recipeId, "webp");

            compressedImage = compressImage(file);
            String result = cosManager.uploadFile(uploadFilePath, compressedImage);

            Recipe recipe = this.getById(recipeId);
            if (recipe == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "菜谱不存在");
            }
            recipe.setCoverImage(result);
            boolean update = this.updateById(recipe);
            if (!update) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新菜谱图片失败");
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

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
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
            log.info("图片压缩成功，原始大小: {} KB, 压缩后大小: {} KB", oldFile.length() / 1024, newFile.length() / 1024);
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

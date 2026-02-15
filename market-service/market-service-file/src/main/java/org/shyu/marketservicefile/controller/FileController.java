package org.shyu.marketservicefile.controller;

import lombok.extern.slf4j.Slf4j;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicefile.config.MinioConfig;
import org.shyu.marketservicefile.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private MinioConfig minioConfig;

    /**
     * 上传商品图片
     */
    @PostMapping("/upload/product")
    public Result<String> uploadProductImage(@RequestParam("file") MultipartFile file) {
        log.info("上传商品图片: {}", file.getOriginalFilename());

        // 1. 校验文件
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 2. 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        // 3. 校验文件大小（最大5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过5MB");
        }

        // 4. 上传文件
        String url = fileService.uploadFile(file, minioConfig.getBucket().getProduct());

        return Result.success("上传成功", url);
    }

    /**
     * 批量上传商品图片（最多9张）
     */
    @PostMapping("/upload/product/batch")
    public Result<List<String>> uploadProductImages(@RequestParam("files") MultipartFile[] files) {
        log.info("批量上传商品图片，数量: {}", files.length);

        // 1. 校验数量
        if (files.length == 0) {
            return Result.error("请选择要上传的文件");
        }
        if (files.length > 9) {
            return Result.error("最多上传9张图片");
        }

        // 2. 校验每个文件
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                return Result.error("存在空文件");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("图片 " + file.getOriginalFilename() + " 大小不能超过5MB");
            }
        }

        // 3. 批量上传
        List<String> urls = fileService.uploadFiles(files, minioConfig.getBucket().getProduct());

        return Result.success("上传成功", urls);
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("上传用户头像: {}", file.getOriginalFilename());

        // 1. 校验文件
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 2. 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        // 3. 校验文件大小（最大2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("头像大小不能超过2MB");
        }

        // 4. 上传文件
        String url = fileService.uploadFile(file, minioConfig.getBucket().getAvatar());

        return Result.success("上传成功", url);
    }

    /**
     * 上传仲裁证据
     */
    @PostMapping("/upload/arbitration")
    public Result<String> uploadArbitrationEvidence(@RequestParam("file") MultipartFile file) {
        log.info("上传仲裁证据: {}", file.getOriginalFilename());

        // 1. 校验文件
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 2. 校验文件大小（最大10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("文件大小不能超过10MB");
        }

        // 3. 上传文件
        String url = fileService.uploadFile(file, minioConfig.getBucket().getArbitration());

        return Result.success("上传成功", url);
    }
}


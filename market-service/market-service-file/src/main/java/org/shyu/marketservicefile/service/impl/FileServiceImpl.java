package org.shyu.marketservicefile.service.impl;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketservicefile.config.MinioConfig;
import org.shyu.marketservicefile.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务实现
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        InputStream inputStream = null;
        try {
            // 1. 确保桶存在
            ensureBucketExists(bucketName);

            // 2. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

            // 3. 获取文件流
            inputStream = file.getInputStream();

            // 4. 上传文件到 MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 5. 返回文件访问URL
            String fileUrl = String.format("%s/%s/%s",
                    minioConfig.getEndpoint(), bucketName, fileName);

            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        } finally {
            // 确保关闭输入流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    log.warn("关闭文件流失败: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files, String bucketName) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = uploadFile(file, bucketName);
            urls.add(url);
        }
        return urls;
    }

    @Override
    public void deleteFile(String fileName, String bucketName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("文件删除成功: {}/{}", bucketName, fileName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadFile(String fileName, String bucketName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String fileName, String bucketName) {
        return String.format("%s/%s/%s",
                minioConfig.getEndpoint(), bucketName, fileName);
    }

    /**
     * 确保桶存在，不存在则创建
     */
    private void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                // 创建桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );

                // 设置桶为公开读（重要！）
                String policy = "{\n" +
                        "  \"Version\": \"2012-10-17\",\n" +
                        "  \"Statement\": [\n" +
                        "    {\n" +
                        "      \"Effect\": \"Allow\",\n" +
                        "      \"Principal\": {\"AWS\": [\"*\"]},\n" +
                        "      \"Action\": [\"s3:GetObject\"],\n" +
                        "      \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build()
                );

                log.info("创建并配置桶成功: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查或创建桶失败: {}", e.getMessage(), e);
            throw new BusinessException("桶操作失败: " + e.getMessage());
        }
    }
}


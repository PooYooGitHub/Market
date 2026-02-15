package org.shyu.marketservicefile.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传单个文件
     * @param file 文件
     * @param bucketName 桶名称
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String bucketName);

    /**
     * 批量上传文件
     * @param files 文件数组
     * @param bucketName 桶名称
     * @return 文件访问URL列表
     */
    List<String> uploadFiles(MultipartFile[] files, String bucketName);

    /**
     * 删除文件
     * @param fileName 文件名
     * @param bucketName 桶名称
     */
    void deleteFile(String fileName, String bucketName);

    /**
     * 下载文件
     * @param fileName 文件名
     * @param bucketName 桶名称
     * @return 文件流
     */
    InputStream downloadFile(String fileName, String bucketName);

    /**
     * 获取文件访问URL
     * @param fileName 文件名
     * @param bucketName 桶名称
     * @return 访问URL
     */
    String getFileUrl(String fileName, String bucketName);
}


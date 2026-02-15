package org.shyu.marketservicefile.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * MinIO 配置类
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioConfig {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private BucketConfig bucket;

    @Data
    public static class BucketConfig {
        private String product;
        private String avatar;
        private String arbitration;
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 初始化MinIO，创建必需的bucket
     */
    @PostConstruct
    public void initBuckets() {
        try {
            MinioClient client = minioClient();
            log.info("开始初始化MinIO，端点: {}", endpoint);

            // 初始化所有bucket
            createBucketIfNotExists(client, bucket.getProduct());
            createBucketIfNotExists(client, bucket.getAvatar());
            createBucketIfNotExists(client, bucket.getArbitration());

            log.info("MinIO初始化完成");
        } catch (Exception e) {
            log.error("MinIO初始化失败: {}", e.getMessage(), e);
            // 不抛出异常，让应用继续启动
        }
    }

    /**
     * 创建bucket（如果不存在）
     */
    private void createBucketIfNotExists(MinioClient client, String bucketName) {
        try {
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                // 创建bucket
                client.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("创建bucket成功: {}", bucketName);

                // 设置bucket为公开读
                String policy = String.format(
                        "{\"Version\":\"2012-10-17\"," +
                        "\"Statement\":[{" +
                        "\"Effect\":\"Allow\"," +
                        "\"Principal\":{\"AWS\":[\"*\"]}," +
                        "\"Action\":[\"s3:GetObject\"]," +
                        "\"Resource\":[\"arn:aws:s3:::%s/*\"]" +
                        "}]}", bucketName
                );

                client.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build()
                );
                log.info("设置bucket公开读权限成功: {}", bucketName);
            } else {
                log.info("Bucket已存在: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("创建或配置bucket失败: {}, 错误: {}", bucketName, e.getMessage());
        }
    }
}

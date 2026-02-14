package org.shyu.marketserviceuser.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户查询DTO
 */
@Data
public class UserQueryDTO {

    /**
     * 当前页码
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 关键词（用户名、昵称、手机号）
     */
    private String keyword;

    /**
     * 状态：0-已删除，1-正常，2-禁用
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}


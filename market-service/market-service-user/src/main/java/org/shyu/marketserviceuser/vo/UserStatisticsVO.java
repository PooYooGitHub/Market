package org.shyu.marketserviceuser.vo;

import lombok.Data;

/**
 * 用户统计VO
 */
@Data
public class UserStatisticsVO {
    /**
     * 总用户数
     */
    private Long totalCount;

    /**
     * 正常用户数（status=1）
     */
    private Long activeCount;

    /**
     * 禁用用户数（status=0）
     */
    private Long disabledCount;

    /**
     * 已删除用户数（物理删除，无法统计）
     */
    private Long deletedCount;

    /**
     * 今日注册数
     */
    private Long todayRegister;
}


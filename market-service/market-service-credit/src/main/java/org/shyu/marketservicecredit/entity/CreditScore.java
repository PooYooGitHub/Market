package org.shyu.marketservicecredit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 信用分实体类
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_credit_score")
public class CreditScore {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 信用分
     */
    private Integer score;

    /**
     * 信用等级
     */
    private String level;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
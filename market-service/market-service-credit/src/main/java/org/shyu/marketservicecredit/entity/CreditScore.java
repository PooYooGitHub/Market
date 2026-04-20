package org.shyu.marketservicecredit.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * User credit aggregate.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_credit_score")
public class CreditScore {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer score;

    private String level;

    private String badgeCode;

    private String badgeName;

    private String badgeColor;

    private String badgeDesc;

    private Integer highTrust;

    private Integer validTradeCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

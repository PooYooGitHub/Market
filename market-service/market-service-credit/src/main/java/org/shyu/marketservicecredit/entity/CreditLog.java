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
 * Credit score change log.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_credit_log")
public class CreditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String changeType;

    /**
     * Raw score delta before decay.
     */
    private Integer rawScoreChange;

    /**
     * Effective score delta after decay and clamp.
     */
    private Integer scoreChange;

    private Integer beforeScore;

    private Integer afterScore;

    private Long relatedId;

    private String reason;

    private String eventKey;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

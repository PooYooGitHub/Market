package org.shyu.marketservicecredit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 信用分变更日志实体类
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_credit_log")
public class CreditLog {

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
     * 变更类型
     */
    private String changeType;

    /**
     * 分数变化
     */
    private Integer scoreChange;

    /**
     * 变更前分数
     */
    private Integer beforeScore;

    /**
     * 变更后分数
     */
    private Integer afterScore;

    /**
     * 关联ID（如评价ID、订单ID等）
     */
    private Long relatedId;

    /**
     * 变更原因描述
     */
    private String reason;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        EVALUATION("EVALUATION", "评价"),
        PENALTY("PENALTY", "惩罚"),
        REWARD("REWARD", "奖励"),
        INIT("INIT", "初始化");

        private final String code;
        private final String desc;

        ChangeType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
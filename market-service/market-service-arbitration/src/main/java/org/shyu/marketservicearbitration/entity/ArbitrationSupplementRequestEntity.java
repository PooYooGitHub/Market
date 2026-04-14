package org.shyu.marketservicearbitration.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_arbitration_supplement_request")
public class ArbitrationSupplementRequestEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("arbitration_id")
    private Long arbitrationId;

    @TableField("requested_by")
    private Long requestedBy;

    /**
     * BUYER / SELLER / BOTH
     */
    @TableField("target_party")
    private String targetParty;

    @TableField("required_items")
    private String requiredItems;

    @TableField("remark")
    private String remark;

    @TableField("due_time")
    private LocalDateTime dueTime;

    /**
     * 0-PENDING 1-SUBMITTED 2-EXPIRED 3-CANCELED
     */
    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

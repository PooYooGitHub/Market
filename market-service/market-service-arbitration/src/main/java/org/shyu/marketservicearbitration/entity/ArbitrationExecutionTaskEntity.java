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
@TableName("t_arbitration_execution_task")
public class ArbitrationExecutionTaskEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("arbitration_id")
    private Long arbitrationId;

    @TableField("dispute_id")
    private Long disputeId;

    @TableField("order_id")
    private Long orderId;

    @TableField("execution_type")
    private String executionType;

    @TableField("execution_status")
    private String executionStatus;

    @TableField("payload")
    private String payload;

    @TableField("result_message")
    private String resultMessage;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("finish_time")
    private LocalDateTime finishTime;
}


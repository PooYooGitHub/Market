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
@TableName("t_arbitration_evidence_submission")
public class ArbitrationEvidenceSubmissionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("arbitration_id")
    private Long arbitrationId;

    @TableField("supplement_request_id")
    private Long supplementRequestId;

    @TableField("submitter_id")
    private Long submitterId;

    /**
     * BUYER / SELLER / SYSTEM
     */
    @TableField("submitter_role")
    private String submitterRole;

    @TableField("claim_text")
    private String claimText;

    @TableField("fact_text")
    private String factText;

    /**
     * JSON array
     */
    @TableField("evidence_urls")
    private String evidenceUrls;

    @TableField("note")
    private String note;

    @TableField("is_late")
    private Integer isLate;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

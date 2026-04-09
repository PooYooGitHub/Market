package org.shyu.marketapiarbitration.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 处理仲裁DTO
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Data
public class HandleArbitrationDTO {

    /**
     * 处理动作：accept(接受申请), reject(驳回申请), resolve(结案)
     */
    @NotBlank(message = "处理动作不能为空")
    private String action;

    /**
     * 处理结果说明
     */
    @Size(min = 5, max = 1000, message = "处理结果说明长度必须在5-1000个字符之间")
    private String result;

    /**
     * 备注信息
     */
    @Size(max = 255, message = "备注信息不能超过255个字符")
    private String remark;

    /**
     * 信用分调整 - 申请人信用分变化
     */
    private Integer applicantCreditChange = 0;

    /**
     * 信用分调整 - 被申诉人信用分变化
     */
    private Integer respondentCreditChange = 0;
}
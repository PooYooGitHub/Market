package org.shyu.marketapiarbitration.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 创建仲裁申请DTO
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Data
public class CreateArbitrationDTO {

    /**
     * 关联订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 被申诉人ID（对方用户ID）
     */
    @NotNull(message = "被申诉人ID不能为空")
    private Long respondentId;

    /**
     * 仲裁原因
     */
    @NotBlank(message = "仲裁原因不能为空")
    private String reason;

    /**
     * 详细描述
     */
    @Size(min = 10, max = 1000, message = "详细描述长度必须在10-1000个字符之间")
    private String description;

    /**
     * 证据材料URL列表
     */
    private List<String> evidenceUrls;

    /**
     * 联系方式（可选）
     */
    @Size(max = 100, message = "联系方式不能超过100个字符")
    private String contactInfo;
}
package org.shyu.marketservicearbitration.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SupplementRequestDTO {

    @NotNull(message = "仲裁ID不能为空")
    private Long arbitrationId;

    @NotBlank(message = "补证对象不能为空")
    private String targetParty;

    @NotBlank(message = "补证要求不能为空")
    private String requiredItems;

    private String remark;

    /**
     * 补证时限（小时），默认 48。
     */
    private Integer dueHours = 48;
}

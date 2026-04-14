package org.shyu.marketservicearbitration.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SupplementSubmitDTO {

    @NotNull(message = "仲裁ID不能为空")
    private Long arbitrationId;

    /**
     * 可为空；为空时系统会尝试匹配当前待补证请求。
     */
    private Long supplementRequestId;

    private String claim;

    private String facts;

    private List<String> evidenceUrls;

    private String note;
}

package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArbitrationSupplementRequestVO {

    private Long id;

    private Long arbitrationId;

    private Long requestedBy;

    private String targetParty;

    private String requiredItems;

    private String remark;

    private LocalDateTime dueTime;

    private Integer status;

    private String statusDesc;

    private LocalDateTime createTime;
}

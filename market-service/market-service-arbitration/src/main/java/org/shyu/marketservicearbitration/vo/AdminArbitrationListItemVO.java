package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminArbitrationListItemVO {

    private Long id;

    private String caseNumber;

    private Integer status;

    private String statusLabel;

    private String reason;

    private String reasonLabel;

    private String title;

    private String description;

    private String result;

    private String decisionType;

    private String decisionTypeLabel;

    private String executionType;

    private String executionTypeLabel;

    private String executionStatus;

    private String executionStatusLabel;

    private Long orderId;

    private String orderNo;

    private BigDecimal orderAmount;

    private Long applicantId;

    private String applicantName;

    private Long respondentId;

    private String respondentName;

    private Long handlerId;

    private String handlerName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

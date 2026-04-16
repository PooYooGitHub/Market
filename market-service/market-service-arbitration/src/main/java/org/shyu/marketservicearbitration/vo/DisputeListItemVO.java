package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DisputeListItemVO {

    private Long id;

    private Long orderId;

    private Long productId;

    private String reason;

    private String requestType;

    private BigDecimal expectedAmount;

    private String status;

    private String statusLabel;

    private String sellerResponseType;

    private String finalDecisionType;

    private String finalExecutionStatus;

    private String finalResultDescription;

    private LocalDateTime expireTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Boolean canEscalate;
}

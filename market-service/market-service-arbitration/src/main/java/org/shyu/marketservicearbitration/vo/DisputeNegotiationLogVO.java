package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DisputeNegotiationLogVO {

    private Long id;

    private Integer roundNo;

    private Long actorId;

    private String actorRole;

    private String actionType;

    private String actionLabel;

    private String content;

    private BigDecimal amount;

    private LocalDateTime createTime;
}


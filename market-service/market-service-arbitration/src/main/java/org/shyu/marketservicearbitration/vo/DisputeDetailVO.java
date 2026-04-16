package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DisputeDetailVO {

    private Long id;

    private Long orderId;

    private Long productId;

    private Long buyerId;

    private Long sellerId;

    private String reason;

    private String factDescription;

    private String requestType;

    private String requestDescription;

    private BigDecimal expectedAmount;

    private String status;

    private String statusLabel;

    private Integer currentRound;

    private Long escalatedArbitrationId;

    private LocalDateTime expireTime;

    private Long countdownSeconds;

    private String negotiationSummary;

    private String executionStatus;

    private String executionStatusLabel;

    private String executionRemark;

    private SellerProposalVO sellerProposal;

    private List<DisputeEvidenceVO> evidenceList = new ArrayList<>();

    private List<DisputeNegotiationLogVO> negotiationLogs = new ArrayList<>();

    private List<DisputeChatSummaryVO> chatSummary = new ArrayList<>();

    private Boolean canSellerRespond;

    private Boolean canBuyerConfirm;

    private Boolean canEscalate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

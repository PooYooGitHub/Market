package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class AdminArbitrationDetailVO {

    private Long id;

    private String caseNumber;

    private Integer status;

    private String statusLabel;

    private String reason;

    private String reasonLabel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long orderId;

    private String orderNo;

    private BigDecimal orderAmount;

    private Long productId;

    private String productName;

    private BigDecimal productPrice;

    private String productImage;

    private Long sourceDisputeId;

    private Long applicantId;

    private String applicantName;

    private Long respondentId;

    private String respondentName;

    private Long handlerId;

    private String handlerName;

    private String buyerClaim;

    private String sellerClaim;

    private String platformFocus;

    private String arbitrationRequest;

    private String negotiationSummary;

    private List<ArbitrationEvidenceVO> applicantEvidence = new ArrayList<>();

    private List<ArbitrationEvidenceVO> respondentEvidence = new ArrayList<>();

    private List<ArbitrationEvidenceVO> systemEvidence = new ArrayList<>();

    private List<ArbitrationChatSummaryVO> chatSummary = new ArrayList<>();

    private Map<String, Object> orderSnapshot = new LinkedHashMap<>();

    private Map<String, Object> productSnapshot = new LinkedHashMap<>();

    private List<ArbitrationTimelineVO> timeline = new ArrayList<>();

    private String decisionRemark;

    private String rejectReason;

    private Boolean canAccept;

    private Boolean canComplete;

    private Boolean canReject;

    private Boolean readonly;
}

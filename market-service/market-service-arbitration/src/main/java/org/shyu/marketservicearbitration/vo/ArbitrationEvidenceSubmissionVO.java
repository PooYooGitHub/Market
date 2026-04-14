package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArbitrationEvidenceSubmissionVO {

    private Long id;

    private Long supplementRequestId;

    private Long submitterId;

    private String submitterRole;

    private String claim;

    private String facts;

    private List<String> evidenceUrls;

    private String note;

    private Boolean late;

    private LocalDateTime createTime;
}

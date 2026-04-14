package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DisputeEvidenceVO {

    private Long id;

    private String evidenceType;

    private String title;

    private String description;

    private String fileUrl;

    private String thumbnailUrl;

    private String uploaderRole;

    private LocalDateTime createTime;
}


package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArbitrationEvidenceVO {

    private Long id;

    private String type;

    private String title;

    private String description;

    private String url;

    private String thumbnail;

    private LocalDateTime createTime;
}

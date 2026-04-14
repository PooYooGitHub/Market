package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DisputeChatSummaryVO {

    private String speaker;

    private String role;

    private String content;

    private LocalDateTime time;

    private String sourceType;
}


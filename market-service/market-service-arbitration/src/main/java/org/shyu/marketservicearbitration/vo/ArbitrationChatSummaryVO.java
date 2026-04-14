package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArbitrationChatSummaryVO {

    private Long id;

    private String speaker;

    private LocalDateTime time;

    private String content;
}

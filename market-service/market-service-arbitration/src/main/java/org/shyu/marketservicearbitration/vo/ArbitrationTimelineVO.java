package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArbitrationTimelineVO {

    private Long id;

    private String title;

    private String description;

    private String color;

    private LocalDateTime time;
}

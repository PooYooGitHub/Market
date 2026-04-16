package org.shyu.marketapimessage.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageChatHistoryPageDTO {

    private Long total;

    private Long size;

    private Long current;

    private Long pages;

    private List<MessageChatRecordDTO> records = new ArrayList<>();
}

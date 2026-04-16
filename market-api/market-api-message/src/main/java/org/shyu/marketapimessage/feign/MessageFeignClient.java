package org.shyu.marketapimessage.feign;

import org.shyu.marketapimessage.dto.MessageChatHistoryPageDTO;
import org.shyu.marketcommon.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "market-service-message", path = "/message")
public interface MessageFeignClient {

    @GetMapping("/history/{otherUserId}")
    Result<MessageChatHistoryPageDTO> getChatHistory(@RequestHeader("X-User-Id") Long userId,
                                                     @PathVariable("otherUserId") Long otherUserId,
                                                     @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                     @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize);
}

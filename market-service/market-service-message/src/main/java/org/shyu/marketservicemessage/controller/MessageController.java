package org.shyu.marketservicemessage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicemessage.dto.SendMessageDTO;
import org.shyu.marketservicemessage.service.MessageService;
import org.shyu.marketservicemessage.service.OfflineMessageService;
import org.shyu.marketservicemessage.vo.ChatMessageVO;
import org.shyu.marketservicemessage.vo.ConversationVO;
import org.shyu.marketservicemessage.vo.OfflineMessageVO;
import org.shyu.marketservicemessage.websocket.ChatWebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 *
 * 注意：所有接口都通过Gateway访问，Gateway已完成Token验证
 * Gateway会将userId通过 X-User-Id Header 传递过来
 *
 * @author shyu
 * @date 2026-02-15
 */
@Slf4j
@Api(tags = "消息管理")
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired(required = false)
    private OfflineMessageService offlineMessageService;

    /**
     * 发送消息
     */
    @ApiOperation("发送消息")
    @PostMapping("/send")
    public Result<Void> sendMessage(@RequestHeader("X-User-Id") Long userId,
                                    @Valid @RequestBody SendMessageDTO dto) {
        messageService.sendMessage(dto, userId);
        return Result.success();
    }

    /**
     * 获取会话列表
     */
    @ApiOperation("获取会话列表")
    @GetMapping("/conversation/list")
    public Result<List<ConversationVO>> getConversationList(@RequestHeader("X-User-Id") Long userId) {
        List<ConversationVO> list = messageService.getConversationList(userId);
        return Result.success(list);
    }

    /**
     * 获取聊天记录
     */
    @ApiOperation("获取聊天记录")
    @GetMapping("/history/{otherUserId}")
    public Result<Page<ChatMessageVO>> getChatHistory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<ChatMessageVO> page = messageService.getChatHistory(userId, otherUserId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 标记消息为已读
     */
    @ApiOperation("标记消息为已读")
    @PutMapping("/read/{otherUserId}")
    public Result<Void> markAsRead(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long otherUserId) {
        messageService.markAsRead(userId, otherUserId);
        return Result.success();
    }

    /**
     * 获取未读消息总数
     */
    @ApiOperation("获取未读消息总数")
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount(@RequestHeader("X-User-Id") Long userId) {
        Integer count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 删除会话
     */
    @ApiOperation("删除会话")
    @DeleteMapping("/conversation/{conversationId}")
    public Result<Void> deleteConversation(@RequestHeader("X-User-Id") Long userId,
                                           @PathVariable Long conversationId) {
        messageService.deleteConversation(userId, conversationId);
        return Result.success();
    }

    /**
     * 获取在线状态
     */
    @ApiOperation("获取在线状态")
    @GetMapping("/online/{userId}")
    public Result<Map<String, Object>> getOnlineStatus(@PathVariable Long userId) {
        boolean isOnline = ChatWebSocketServer.isUserOnline(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("isOnline", isOnline);
        return Result.success(result);
    }

    /**
     * 获取在线用户数
     */
    @ApiOperation("获取在线用户数")
    @GetMapping("/online/count")
    public Result<Integer> getOnlineCount() {
        int count = ChatWebSocketServer.getOnlineCount();
        return Result.success(count);
    }

    /**
     * 获取离线消息
     */
    @ApiOperation("获取离线消息")
    @GetMapping("/offline")
    public Result<List<OfflineMessageVO>> getOfflineMessages(@RequestHeader("X-User-Id") Long userId) {
        if (offlineMessageService == null) {
            log.warn("OfflineMessageService 不可用，离线消息功能已降级");
            return Result.success(Collections.emptyList());
        }
        List<OfflineMessageVO> messages = offlineMessageService.getOfflineMessages(userId);
        return Result.success(messages);
    }

    /**
     * 清除离线消息
     */
    @ApiOperation("清除离线消息")
    @DeleteMapping("/offline")
    public Result<Void> clearOfflineMessages(@RequestHeader("X-User-Id") Long userId) {
        if (offlineMessageService == null) {
            log.warn("OfflineMessageService 不可用，离线消息功能已降级");
            return Result.success();
        }
        offlineMessageService.clearOfflineMessages(userId);
        return Result.success();
    }

}


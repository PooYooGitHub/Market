package org.shyu.marketservicemessage.websocket;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketservicemessage.service.OfflineMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务器 - 聊天消息
 *
 * @author shyu
 * @since 2026-02-15
 */
@Slf4j
@Component
@ServerEndpoint("/ws/chat/{userId}")
public class ChatWebSocketServer {

    /**
     * 存储所有在线用户的 WebSocket Session
     * key: userId, value: Session
     */
    private static final Map<Long, Session> ONLINE_USERS = new ConcurrentHashMap<>();

    /**
     * 离线消息服务（静态注入）
     */
    private static OfflineMessageService offlineMessageService;

    @Autowired(required = false)
    public void setOfflineMessageService(OfflineMessageService offlineMessageService) {
        ChatWebSocketServer.offlineMessageService = offlineMessageService;
    }

    /**
     * 当前连接的用户ID
     */
    private Long userId;

    /**
     * 连接建立成功调用
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        this.userId = userId;
        ONLINE_USERS.put(userId, session);
        log.info("用户 {} 建立 WebSocket 连接，当前在线人数: {}", userId, ONLINE_USERS.size());

        // 发送连接成功消息
        sendMessage(session, JSONUtil.createObj()
                .set("type", "connect")
                .set("message", "连接成功")
                .toString());

        // 推送离线消息
        pushOfflineMessages(userId);
    }

    /**
     * 推送离线消息给刚上线的用户
     */
    private void pushOfflineMessages(Long userId) {
        try {
            if (offlineMessageService != null) {
                // 异步推送离线消息（避免阻塞连接）
                new Thread(() -> {
                    try {
                        // 延迟1秒，确保WebSocket连接稳定
                        Thread.sleep(1000);

                        int pushedCount = offlineMessageService.pushOfflineMessagesToUser(userId);

                        if (pushedCount > 0) {
                            // 发送推送完成通知
                            Session session = ONLINE_USERS.get(userId);
                            if (session != null && session.isOpen()) {
                                sendMessage(session, JSONUtil.createObj()
                                        .set("type", "offline_push_complete")
                                        .set("count", pushedCount)
                                        .set("message", "您有 " + pushedCount + " 条离线消息")
                                        .toString());
                            }
                        }
                    } catch (Exception e) {
                        log.error("推送离线消息失败: userId={}", userId, e);
                    }
                }).start();
            }
        } catch (Exception e) {
            log.error("启动离线消息推送失败: userId={}", userId, e);
        }
    }

    // ...existing code...

    /**
     * 连接关闭调用
     */
    @OnClose
    public void onClose() {
        ONLINE_USERS.remove(userId);
        log.info("用户 {} 断开 WebSocket 连接，当前在线人数: {}", userId, ONLINE_USERS.size());
    }

    /**
     * 收到客户端消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到用户 {} 的消息: {}", userId, message);
        // 消息处理逻辑在 Service 层，这里只做转发
    }

    /**
     * 发生错误
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户 {} WebSocket 发生错误", userId, error);
    }

    /**
     * 向指定用户发送消息
     */
    public static void sendMessageToUser(Long userId, String message) {
        Session session = ONLINE_USERS.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        } else {
            log.warn("用户 {} 不在线，无法发送消息", userId);
        }
    }

    /**
     * 向所有在线用户广播消息
     */
    public static void broadcastMessage(String message) {
        ONLINE_USERS.values().forEach(session -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        });
    }

    /**
     * 发送消息
     */
    private static void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("发送 WebSocket 消息失败", e);
        }
    }

    /**
     * 判断用户是否在线
     */
    public static boolean isUserOnline(Long userId) {
        return ONLINE_USERS.containsKey(userId);
    }

    /**
     * 获取在线用户数量
     */
    public static int getOnlineCount() {
        return ONLINE_USERS.size();
    }

}


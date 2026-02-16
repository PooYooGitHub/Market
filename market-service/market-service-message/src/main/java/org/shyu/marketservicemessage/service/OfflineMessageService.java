package org.shyu.marketservicemessage.service;

import org.shyu.marketservicemessage.vo.OfflineMessageVO;

import java.util.List;

/**
 * 离线消息服务接口
 *
 * @author shyu
 * @date 2026-02-16
 */
public interface OfflineMessageService {

    /**
     * 获取用户的离线消息
     *
     * @param userId 用户ID
     * @return 离线消息列表
     */
    List<OfflineMessageVO> getOfflineMessages(Long userId);

    /**
     * 清除用户的离线消息
     *
     * @param userId 用户ID
     */
    void clearOfflineMessages(Long userId);

    /**
     * 推送离线消息给刚上线的用户
     *
     * @param userId 用户ID
     * @return 推送的消息数量
     */
    int pushOfflineMessagesToUser(Long userId);
}


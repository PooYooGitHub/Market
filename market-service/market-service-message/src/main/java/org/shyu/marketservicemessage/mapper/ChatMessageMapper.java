package org.shyu.marketservicemessage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.shyu.marketservicemessage.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息 Mapper
 *
 * @author shyu
 * @date 2026-02-15
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}


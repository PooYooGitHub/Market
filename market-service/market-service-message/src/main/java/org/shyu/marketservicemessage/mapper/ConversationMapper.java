package org.shyu.marketservicemessage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.shyu.marketservicemessage.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话 Mapper
 *
 * @author shyu
 * @date 2026-02-15
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}


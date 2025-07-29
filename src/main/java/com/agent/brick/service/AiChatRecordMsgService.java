package com.agent.brick.service;

import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author autoCode
 * @since 2025-07-05
 */
public interface AiChatRecordMsgService  {

    Map<Long,ChatRecordMsgJsonDto> queryMsg(List<Long> ids);
}

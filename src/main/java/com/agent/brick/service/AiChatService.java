package com.agent.brick.service;

import com.agent.brick.controller.request.AiChatReq;
import com.agent.brick.model.AiChat;
import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;
import com.agent.brick.pojo.vo.AiChatVO;
import com.agent.brick.pojo.vo.PageVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author autoCode
 * @since 2025-07-05
 */
public interface AiChatService {

    /**
     * 根据id获取chat
     * @param chatId id
     * @return chat
     */
    AiChat queryChatById(Long chatId);

    void insert(AiChat chat);

    /**
     * 分页获取用户对话记录
     * @param aiChatReq req
     * @return page
     */
    PageVO<AiChatVO> queryChatPage(AiChatReq aiChatReq);

    /**
     * 查询对话详情记录
     * @param chatId 对话id
     * @return record list
     */
    List<ChatRecordMsgJsonDto> queryChatRecordList(Long chatId);
}

package com.agent.brick.service;

import com.agent.brick.model.AiChatRecord;
import com.agent.brick.pojo.dto.AiChatRecordDto;
import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author autoCode
 * @since 2025-07-05
 */
public interface AiChatRecordService  {

    /**
     * 获取name上一次的助手id
     * @param chatId chatID
     * @param name name
     * @return parentId
     */
    Long queryNameLastParentId(Long chatId, String name);


    /**
     * 获取对话记录最后一个
     * @param chatId chatId
     * @param name 名称
     * @return charRecord
     */
    AiChatRecord queryRecordLastOne(Long chatId, String name);

    /**
     * 根据 父id获取记录
     * @param parentId 父id
     * @return 记录
     */
    AiChatRecord queryRecordByParentId(Long parentId);

    void insert(AiChatRecord aiChatRecord);

    void insert(AiChatRecord aiChatRecord, ChatRecordMsgJsonDto msgJsonDto);

    /**
     * 从低位记录查询对话列表
     * 例:A->B->A->B->A->B
     * 低位则查询所有的B,通过自身的parentId获取到所有的A,形成chain
     * @param chatId 对话id
     * @param name 名称
     * @return 对话记录传输类列表 包含消息
     */
    List<AiChatRecordDto> queryListForSub(Long chatId, String name);

    /**
     * 根据chatID name 查询记录列表
     * @param chatId 对话id
     * @param name 主体名称
     * @return 对话列表
     */
    List<AiChatRecord> queryList(Long chatId, String name);

    List<AiChatRecord> queryList(List<Long> ids);

    List<AiChatRecord> queryListByParentIds(List<Long> parentIds);

    /**
     * 从高位记录查询对话列表
     * 例:A->B->A->B->A->B
     * 高位则查询所有的A,通过自身的ID获取到所有的B,形成chain
     * @param chatId 对话id
     * @param name 名称
     * @return 对话记录传输类列表 包含消息
     */
    List<AiChatRecordDto> queryListForSuper(Long chatId, String name);
}

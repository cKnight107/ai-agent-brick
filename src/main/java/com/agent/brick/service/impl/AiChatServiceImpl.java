package com.agent.brick.service.impl;

import com.agent.brick.base.BaseService;
import com.agent.brick.controller.request.AiChatReq;
import com.agent.brick.mapper.AiChatMapper;
import com.agent.brick.model.AiChat;
import com.agent.brick.pojo.dto.AiChatRecordDto;
import com.agent.brick.pojo.dto.SysCacheUserDto;
import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;
import com.agent.brick.pojo.vo.AiChatVO;
import com.agent.brick.pojo.vo.PageVO;
import com.agent.brick.service.AiChatRecordService;
import com.agent.brick.service.AiChatService;
import com.agent.brick.util.ConvertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author autoCode
 * @since 2025-07-05
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiChatServiceImpl extends BaseService implements AiChatService {
    private final AiChatMapper aiChatMapper;

    private final AiChatRecordService aiChatRecordService;

    @Override
    public AiChat queryChatById(Long chatId) {
        return aiChatMapper.selectById(chatId);
    }

    @Override
    public void insert(AiChat chat) {
        aiChatMapper.insert(chat);
    }

    @Override
    public PageVO<AiChatVO> queryChatPage(AiChatReq aiChatReq) {
        SysCacheUserDto adminUserInfo = getAdminUserInfo();
        return select(aiChatMapper)
                .where()
                .eq(AiChat::getUserId,adminUserInfo.getId())
                .orderDesc(AiChat::getCreateTime)
                .page(aiChatReq, AiChatVO.class);
    }

    @Override
    public List<ChatRecordMsgJsonDto> queryChatRecordList(Long chatId) {
        SysCacheUserDto adminUserInfo = getAdminUserInfo();
        List<AiChatRecordDto> recordList = aiChatRecordService.queryListForSuper(chatId, adminUserInfo.getAccountNo());
        return ConvertUtils.toFieldList(recordList,AiChatRecordDto::getMsgJsonDto);
    }
}

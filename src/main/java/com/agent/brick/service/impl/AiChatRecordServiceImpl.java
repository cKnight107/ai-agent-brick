package com.agent.brick.service.impl;

import com.agent.brick.base.BaseDO;
import com.agent.brick.base.BaseService;
import com.agent.brick.mapper.AiChatRecordMapper;
import com.agent.brick.mapper.AiChatRecordMsgMapper;
import com.agent.brick.model.AiChatRecord;
import com.agent.brick.model.AiChatRecordMsg;
import com.agent.brick.pojo.dto.AiChatRecordDto;
import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;
import com.agent.brick.service.AiChatRecordMsgService;
import com.agent.brick.service.AiChatRecordService;
import com.agent.brick.util.ConvertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author autoCode
 * @since 2025-07-05
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiChatRecordServiceImpl extends BaseService implements AiChatRecordService {
    private final AiChatRecordMapper aiChatRecordMapper;

    private final AiChatRecordMsgMapper aiChatRecordMsgMapper;

    private final AiChatRecordMsgService aiChatRecordMsgService;

    @Override
    public Long queryNameLastParentId(Long chatId, String name) {
        //1. 获取当前用户对话的最后一个记录
        AiChatRecord aiChatRecord = queryRecordLastOne(chatId, name);
        if (Objects.isNull(aiChatRecord)) {
            return 0L;
        }
        //2. 获取parentId 为此记录id的记录 就是当前用户记录的parent
        AiChatRecord parent = queryRecordByParentId(aiChatRecord.getId());
        return parent.getId();
    }

    @Override
    public AiChatRecord queryRecordLastOne(Long chatId, String name) {
        return where(aiChatRecordMapper)
                .eq(AiChatRecord::getChatId, chatId)
                .eq(AiChatRecord::getName, name)
                .orderDesc(BaseDO::getId)
                .limitOne();
    }

    @Override
    public AiChatRecord queryRecordByParentId(Long parentId) {
        return where(aiChatRecordMapper)
                .eq(AiChatRecord::getParentId, parentId)
                .one();
    }

    @Override
    public void insert(AiChatRecord aiChatRecord) {
        aiChatRecordMapper.insert(aiChatRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void insert(AiChatRecord aiChatRecord, ChatRecordMsgJsonDto msgJsonDto) {
        //msg 入库
        AiChatRecordMsg chatRecordMsg = AiChatRecordMsg.builder().msgDetail(msgJsonDto).build();
        aiChatRecordMsgMapper.insert(chatRecordMsg);
        aiChatRecord.setMsgId(chatRecordMsg.getId());
        //record 入库
        insert(aiChatRecord);
    }

    @Override
    public List<AiChatRecordDto> queryListForSub(Long chatId, String name) {
        //获取低位记录
        List<AiChatRecord> subRecordList = queryList(chatId, name);
        if (CollectionUtils.isEmpty(subRecordList)) {
            return new ArrayList<>();
        }
        //获取对应高位记录
        List<AiChatRecord> superRecordList = queryList(ConvertUtils.toFieldList(subRecordList, AiChatRecord::getParentId));
        //组装chain
        List<AiChatRecord> recordChain = processRecordChain(superRecordList, subRecordList);
        return processRecordChainMsg(recordChain);
    }

    public List<AiChatRecordDto> processRecordChainMsg(List<AiChatRecord> recordChain) {
        //获取msg
        Map<Long, ChatRecordMsgJsonDto> msgMap = aiChatRecordMsgService.queryMsg(ConvertUtils.toFieldList(recordChain, AiChatRecord::getMsgId));
        //组装
        return recordChain.stream().map(obj -> {
            AiChatRecordDto aiChatRecordDto = ConvertUtils.beanProcess(obj, AiChatRecordDto.class);
            aiChatRecordDto.setMsgJsonDto(msgMap.getOrDefault(obj.getMsgId(), ChatRecordMsgJsonDto.builder().msg("").build()));
            return aiChatRecordDto;
        }).collect(Collectors.toList());
    }

    /**
     * 处理为关系链
     *
     * @param superList 高位
     * @param subList   低位
     * @return chian
     */
    private List<AiChatRecord> processRecordChain(List<AiChatRecord> superList, List<AiChatRecord> subList) {
        List<AiChatRecord> result = new ArrayList<>();
        Map<Long, AiChatRecord> subParentMap = hasMap(subList, AiChatRecord::getParentId, v -> v);
        for (AiChatRecord superRecord : superList) {
            result.add(superRecord);
            AiChatRecord subRecord = subParentMap.get(superRecord.getId());
            if (Objects.nonNull(subRecord)) {
                result.add(subRecord);
            }
        }
        return result;
    }

    @Override
    public List<AiChatRecord> queryList(Long chatId, String name) {
        return where(aiChatRecordMapper)
                .eq(AiChatRecord::getChatId, chatId)
                .eq(AiChatRecord::getName, name)
                .list();
    }

    @Override
    public List<AiChatRecord> queryList(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return where(aiChatRecordMapper)
                .in(BaseDO::getId, ids)
                .list();
    }

    @Override
    public List<AiChatRecord> queryListByParentIds(List<Long> parentIds) {
        if (CollectionUtils.isEmpty(parentIds)) {
            return List.of();
        }
        return where(aiChatRecordMapper)
                .in(AiChatRecord::getParentId, parentIds)
                .list();
    }

    @Override
    public List<AiChatRecordDto> queryListForSuper(Long chatId, String name) {
        //获取高位记录
        List<AiChatRecord> superRecordList = queryList(chatId, name);
        if (CollectionUtils.isEmpty(superRecordList)) {
            return List.of();
        }
        List<Long> superIds = ConvertUtils.toFieldList(superRecordList, BaseDO::getId);
        //获取低位记录
        List<AiChatRecord> subRecordList = queryListByParentIds(superIds);
        //处理为chain
        List<AiChatRecord> recordChain = processRecordChain(superRecordList, subRecordList);
        return processRecordChainMsg(recordChain);
    }
}

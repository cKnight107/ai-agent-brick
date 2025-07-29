package com.agent.brick.service.impl;

import com.agent.brick.base.BaseDO;
import com.agent.brick.base.BaseService;
import com.agent.brick.mapper.AiChatRecordMsgMapper;
import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;
import com.agent.brick.service.AiChatRecordMsgService;
import com.agent.brick.util.JSONUtils;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
public class AiChatRecordMsgServiceImpl extends BaseService implements AiChatRecordMsgService {
    private final AiChatRecordMsgMapper aiChatRecordMsgMapper;

    @Override
    public Map<Long,ChatRecordMsgJsonDto> queryMsg(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return Map.of();
        }
        return where(aiChatRecordMsgMapper)
                .in(BaseDO::getId, ids)
                .hasMap(
                        BaseDO::getId,
                        v-> JSONObject.parseObject(v.getMsgDetail().toString(),ChatRecordMsgJsonDto.class)
                );
    }
}

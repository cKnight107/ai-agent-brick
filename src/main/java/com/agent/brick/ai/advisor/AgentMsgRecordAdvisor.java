package com.agent.brick.ai.advisor;

import com.agent.brick.model.AiChatRecord;
import com.agent.brick.pojo.dto.AgentMsgDto;
import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;
import com.agent.brick.service.AiChatRecordService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.Objects;

/**
 * <p>
 * 智能体消息记录拦截器
 * </p>
 *
 * @author cKnight
 * @since 2025/7/7
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class AgentMsgRecordAdvisor implements BaseAdvisor {
    private int order = 1;

    private AgentMsgDto agentMsgDto;

    private AiChatRecordService recordService;

    private Long parentId = 0L;

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        if (Objects.isNull(agentMsgDto)) {
            log.warn("智能体消息记录拦截器，before未发现agentMsgDto");
            return chatClientRequest;
        }
        AiChatRecord aiChatRecord = recordService.queryRecordLastOne(agentMsgDto.getChatId(), agentMsgDto.getAgentName());
        if (Objects.nonNull(aiChatRecord)) {
            parentId = aiChatRecord.getId();
        }
        AiChatRecord chatRecord = AiChatRecord.builder()
                .chatId(agentMsgDto.getChatId())
                .parentId(parentId)
                .name(agentMsgDto.getUserName())
                .build();
        parentId = IdWorker.getId();
        chatRecord.setId(parentId);
        recordService.insert(chatRecord, ChatRecordMsgJsonDto.builder().msg(agentMsgDto.getQuery()).build());
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        if (this.parentId.equals(0L)){
            return chatClientResponse;
        }
        Long chatId = agentMsgDto.getChatId();
        if (Objects.nonNull(chatClientResponse.chatResponse()) &&
                Objects.nonNull(chatClientResponse.chatResponse().getResult()) &&
                Objects.nonNull(chatClientResponse.chatResponse().getResult().getOutput())) {
            AssistantMessage output = chatClientResponse.chatResponse().getResult().getOutput();
            //record msg入库
            recordService.insert(
                    AiChatRecord.builder()
                            .name(agentMsgDto.getAgentName())
                            .chatId(chatId)
                            .parentId(this.parentId)
                            .build(),
                    ChatRecordMsgJsonDto.builder().msg(output.getText()).build()
            );
        }
        return chatClientResponse;
    }
}

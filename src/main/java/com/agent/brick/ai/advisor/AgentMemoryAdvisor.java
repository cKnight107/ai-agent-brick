package com.agent.brick.ai.advisor;

import com.agent.brick.ai.prompt.PromptConstants;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.pojo.dto.AgentMsgDto;
import com.agent.brick.pojo.dto.AiChatRecordDto;
import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;
import com.agent.brick.service.AiChatRecordService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.StringTemplate.STR;

/**
 * <p>
 * 智能体记忆拦截器
 * </p>
 *
 * @author cKnight
 * @since 2025/7/7
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class AgentMemoryAdvisor implements BaseAdvisor {

    private int order = 10;

    private AgentMsgDto agentMsgDto;

    private AiChatRecordService recordService;

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        if (Objects.isNull(agentMsgDto)) {
            log.warn("智能体记忆拦截器，before未发现agentMsgDto");
            return chatClientRequest;
        }
        //1. 获取记忆
        List<AiChatRecordDto> recordList = recordService.queryListForSub(agentMsgDto.getChatId(), agentMsgDto.getAgentName());
        recordList.add(
                AiChatRecordDto.builder()
                        .name(GlobalConstants.ASSISTANT_AGENT_NAME)
                        .msgJsonDto(ChatRecordMsgJsonDto.builder().msg(agentMsgDto.getQuery()).build())
                        .build());
        //2. 组装记忆
        String memory = recordList.stream()
                .map(m -> STR."\{m.getName()}:\{m.getMsgJsonDto().getMsg()}")
                .collect(Collectors.joining(System.lineSeparator()));

        //3. 组装原始指令
        SystemMessage systemMessage = chatClientRequest.prompt().getSystemMessage();
        String augmentedSystemText = PromptConstants.DEFAULT_CHAT_MEMORY_TEMPLATE.render(
                Map.of(GlobalConstants.INSTRUCTIONS, systemMessage.getText(),
                        GlobalConstants.MEMORY, memory)
        );

        //4. 创建一个新的chatClientRequest
        ChatClientRequest processedChatClientRequest = chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentSystemMessage(augmentedSystemText))
                .build();

        return processedChatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }
}

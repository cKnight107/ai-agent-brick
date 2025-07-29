package com.agent.brick.ai.tools;

import com.agent.brick.ai.advisor.AgentMemoryAdvisor;
import com.agent.brick.ai.advisor.AgentMsgRecordAdvisor;
import com.agent.brick.ai.agent.AbstractAgent;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.pojo.dto.AgentMsgDto;
import com.agent.brick.service.AiChatRecordService;
import com.agent.brick.util.AiUtil;
import com.agent.brick.util.SpringContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * agent 工具类
 * @since 2025/6/21
 *
 * @author cKnight
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AgentTools {

    private final AiChatRecordService aiChatRecordService;

    @Tool(description = "根据Agent名称调用Agent")
    public String callAgent(@ToolParam(description = "Agent名称") String agentName,
                            @ToolParam(description = "需要解决的问题或者需要完成的任务") String query,
//                            @ToolParam(description = "问题所需要的上下文信息") String context,
                            @ToolParam(description = "对话ID") Long chatId,
                            @ToolParam(description = "调用者名称",required = false) String callName
    ) {
        log.info("调用Agent,名称:{},问题:{},上下文:{},chatID:{},调用者:{}", agentName, query,"",chatId,callName);
        AbstractAgent agent = SpringContextUtils.getBean(agentName, AbstractAgent.class);
        if (Objects.isNull(agent)) {
            return "未获取到Agent，请确认Agent名称是否正确";
        }
        AgentMsgDto agentMsgDto = AgentMsgDto.builder()
                .agentName(agentName)
                .chatId(chatId)
                .userName(StringUtils.isNotEmpty(callName) ? callName : GlobalConstants.ASSISTANT_AGENT_NAME)
                .query(query)
                .build();
        String content = agent
                .chatClient()
                .messages(AiUtil.genAgentUserMessage(query,null))
                .advisors(
                        AgentMemoryAdvisor.builder().agentMsgDto(agentMsgDto).recordService(aiChatRecordService).build(),
                        AgentMsgRecordAdvisor.builder().agentMsgDto(agentMsgDto).recordService(aiChatRecordService).build()
                )
                .call()
                .content();

        return content;
    }
}

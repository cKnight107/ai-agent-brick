package com.agent.brick.ai.agent;

import com.agent.brick.ai.advisor.OwlAssistantAgentMsgRecordAdvisor;
import com.agent.brick.ai.prompt.AgentPromptConstants;
import com.agent.brick.compant.AiComponent;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.util.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * OWL 助手智能体
 * </p>
 *
 * @author cKnight
 * @since 2025/7/13
 */
@Slf4j
public class OwlAssistantAgent extends AbstractAgent{

    public static Builder<OwlAssistantAgent> builder(){return new Builder<>(OwlAssistantAgent.class);}

    @Override
    public ChatClient.ChatClientRequestSpec chatClient(AiReq req) {
        Assert.noNullElements(req.getAgentMessages(),"agent对话记忆不可为空");
        ChatClient.ChatClientRequestSpec requestSpec = ChatClient.create(chatModel).prompt(
                AgentPromptConstants.OWL_ASSISTANT_AGENT_SYSTEM_TEMPLATE
                        .create(Map.of(
                                GlobalConstants.REQUEST, req.toString(),
                                GlobalConstants.QUERY, req.getMessage().getContent(),
                                GlobalConstants.MEMORY, req.getAgentMessages()
                                        .stream()
                                        .map(m -> STR."\{m.getRole()}:\{m.getContent()}")
                                        .collect(Collectors.joining(System.lineSeparator()))
                        ))
        );
        AiComponent aiComponent = SpringContextUtils.getBean(AiComponent.class);
        List<AbstractAgent> allAgent = aiComponent.getAllToolAgent();
        if (CollectionUtils.isNotEmpty(allAgent)) {
            allAgent.forEach(requestSpec::tools);
        }
        requestSpec.advisors(new OwlAssistantAgentMsgRecordAdvisor(req));
        return requestSpec;
    }
}

package com.agent.brick.ai.agent;

import com.agent.brick.ai.advisor.OwlUserAgentMsgRecordAdvisor;
import com.agent.brick.ai.prompt.AgentPromptConstants;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.controller.request.AiReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * OWL 用户智能体
 * </p>
 *
 * @author cKnight
 * @since 2025/7/13
 */
@Slf4j
public class OwlUserAgent extends AbstractAgent{
    public static Builder<OwlUserAgent> builder(){return new Builder<>(OwlUserAgent.class);}

    @Override
    public ChatClient.ChatClientRequestSpec chatClient(AiReq req) {
        Assert.noNullElements(req.getAgentMessages(),"agent对话记忆不可为空");
        ChatClient.ChatClientRequestSpec requestSpec = ChatClient.create(chatModel).prompt(
                AgentPromptConstants.OWL_USER_AGENT_SYSTEM_TEMPLE
                        .create(Map.of(
                                GlobalConstants.REQUEST, req.toString(),
                                GlobalConstants.QUERY, req.getMessage().getContent(),
                                GlobalConstants.MEMORY, req.getAgentMessages()
                                        .stream()
                                        .map(m -> STR."\{m.getRole()}:\{m.getContent()}")
                                        .collect(Collectors.joining(System.lineSeparator()))
                        ))
        );
        requestSpec.advisors(new OwlUserAgentMsgRecordAdvisor(req));
        return requestSpec;
    }
}

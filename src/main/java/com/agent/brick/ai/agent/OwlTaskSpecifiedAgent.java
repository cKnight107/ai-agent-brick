package com.agent.brick.ai.agent;

import com.agent.brick.ai.prompt.AgentPromptConstants;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.controller.request.AiReq;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

/**
 * <p>
 * OWL 任务优化
 * </p>
 *
 * @author cKnight
 * @since 2025/7/13
 */
public class OwlTaskSpecifiedAgent extends AbstractAgent{
    public static Builder<OwlTaskSpecifiedAgent> builder(){
        return new Builder<>(OwlTaskSpecifiedAgent.class);
    }

    @Override
    public ChatClient.ChatClientRequestSpec chatClient(AiReq req) {
        ChatClient.ChatClientRequestSpec chatClientRequestSpec = ChatClient.create(chatModel).prompt(
                AgentPromptConstants.OWL_TASK_SPECIFIED_AGENT_SYSTEM_TEMPLATE.create(
                        Map.of(GlobalConstants.QUERY, req.getMessage().getContent(), GlobalConstants.WORD_LIMIT, req.getWorkLimit())
                )
        );
        return chatClientRequestSpec;
    }
}

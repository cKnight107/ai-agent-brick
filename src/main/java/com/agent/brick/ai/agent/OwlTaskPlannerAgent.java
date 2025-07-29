package com.agent.brick.ai.agent;

import com.agent.brick.ai.prompt.AgentPromptConstants;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.controller.request.AiReq;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

/**
 * <p>
 * OWL 任务规划
 * </p>
 *
 * @author cKnight
 * @since 2025/7/13
 */
public class OwlTaskPlannerAgent extends AbstractAgent{

    public static Builder<OwlTaskPlannerAgent> builder(){return new Builder<>(OwlTaskPlannerAgent.class);}

    @Override
    public ChatClient.ChatClientRequestSpec chatClient(AiReq req) {
        ChatClient.ChatClientRequestSpec chatClientRequestSpec = ChatClient.create(chatModel).prompt(
                AgentPromptConstants.OWL_TASK_PLANNER_AGENT_SYSTEM_TEMPLATE.create(
                        Map.of(GlobalConstants.QUERY, req.getMessage().getContent())
                )
        );
        return chatClientRequestSpec;
    }
}

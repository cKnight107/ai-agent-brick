package com.agent.brick.ai.agent;

import com.agent.brick.ai.advisor.MsgRecordAdvisor;
import com.agent.brick.ai.advisor.RequestChatMemoryAdvisor;
import com.agent.brick.ai.tools.AiTools;
import com.agent.brick.compant.AiComponent;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.util.AiUtil;
import com.agent.brick.util.SpringContextUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 助手 智能体
 * @since 2025/6/20
 *
 * @author cKnight
 */
@NoArgsConstructor
@AllArgsConstructor
public class AssistantAgent extends AbstractAgent {

    private AiReq aiReq;

    public static Builder<AssistantAgent> builder() {
        return new Builder<>(AssistantAgent.class);
    }

    public AssistantAgent setAiReq(AiReq aiReq) {
        this.aiReq = aiReq;
        return this;
    }

    @Override
    public ChatClient.ChatClientRequestSpec chatClient() {
        Assert.notNull(aiReq, "入参不可为空");
        Assert.notNull(aiReq.getMessages(), "入参不可为空");
        ChatClient.ChatClientRequestSpec chatClientRequestSpec = super.chatClient();
        chatClientRequestSpec.messages(AiUtil.genUserPrompt(aiReq.getMessage()).getUserMessage());
        AiComponent aiComponent = SpringContextUtils.getBean(AiComponent.class);
        List<AbstractAgent> allAgent = aiComponent.getAllToolAgent();
        if (CollectionUtils.isNotEmpty(allAgent)) {
            allAgent.forEach(chatClientRequestSpec::tools);
        }
        chatClientRequestSpec.tools(
                SpringContextUtils.getBean(AiTools.class)
        );
        chatClientRequestSpec.advisors(
                new RequestChatMemoryAdvisor(aiReq),
                new MsgRecordAdvisor(aiReq)
        );
        return chatClientRequestSpec;
    }
}

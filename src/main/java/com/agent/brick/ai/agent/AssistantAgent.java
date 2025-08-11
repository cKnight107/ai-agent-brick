package com.agent.brick.ai.agent;

import com.agent.brick.ai.advisor.MsgRecordAdvisor;
import com.agent.brick.ai.advisor.RequestChatMemoryAdvisor;
import com.agent.brick.ai.prompt.WwhPromptEngine;
import com.agent.brick.ai.prompt.constants.PromptShotConstants;
import com.agent.brick.ai.prompt.enums.InputSourceEnum;
import com.agent.brick.ai.prompt.enums.PromptEmus;
import com.agent.brick.ai.tools.AiTools;
import com.agent.brick.compant.AiComponent;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.util.AiUtil;
import com.agent.brick.util.SpringContextUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 助手 智能体
 * @since 2025/6/20
 *
 * @author cKnight
 */
@Slf4j
public class AssistantAgent extends AbstractAgent {

    public static Builder<AssistantAgent> builder() {
        return new Builder<>(AssistantAgent.class);
    }

    @Override
    public ChatClient.ChatClientRequestSpec chatClient(AiReq req) {
        ChatClient.ChatClientRequestSpec chatClientRequestSpec = ChatClient.create(this.chatModel).prompt(getPrompt(req));
        this.tools.forEach(chatClientRequestSpec::tools);
        chatClientRequestSpec.messages(AiUtil.genUserPrompt(req.getMessage()).getUserMessage());
        chatClientRequestSpec.advisors(
//                new RequestChatMemoryAdvisor(req),
                new MsgRecordAdvisor(req)
        );
        return chatClientRequestSpec;
    }

    @Override
    public Prompt getPrompt(AiReq req) {
        WwhPromptEngine wwhPromptEngine = PromptEmus.ASSISTANT_TER_WWH_PROMPT.getPrompt();
        log.info("教师助手prompt请求，指纹:{}",wwhPromptEngine.getPromptFingerprint());
        return wwhPromptEngine.toPrompt(
                Map.of(
                        InputSourceEnum.USER_QUERY.name(),
                        req.getMessage().getContent(),
                        InputSourceEnum.USER_INFO.name(),
                        req.getSysCacheUserDto().toString(),
                        InputSourceEnum.REQUEST_INFO.name(),
                        req.toString(),
                        InputSourceEnum.CHAT_HISTORY.name(),
                        req.getMessages().stream().map(m -> STR."\{m.getRole()}:\{m.getContent()}").collect(Collectors.joining(System.lineSeparator()))
                ));
    }
}
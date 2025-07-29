package com.agent.brick.ai.advisor;

import com.agent.brick.ai.prompt.PromptConstants;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.controller.request.AiMessageReq;
import com.agent.brick.controller.request.AiReq;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 入参记忆 拦截器
 * @since 2025/6/9
 *
 * @author cKnight
 */
@Slf4j
public class RequestChatMemoryAdvisor implements BaseChatMemoryAdvisor {

    /**
     * 聊天接口入参
     */
    private final AiReq req;

    public RequestChatMemoryAdvisor(AiReq req) {
        this.req = req;
    }

    /**
     * 调用 LLM之前
     *
     * @param chatClientRequest 请求参数
     * @param advisorChain      链
     * @return 处理后的请求参数
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        if (Objects.isNull(req) || CollectionUtils.isEmpty(req.getMessages())) {
            log.warn("入参记忆拦截，前置方法，未发现req直接返回");
            return chatClientRequest;
        }
        //1. 获取记忆
        List<AiMessageReq> messages = req.getMessages();

        //2. 组装记忆
        String memory = messages.stream()
                .filter(m -> MessageType.USER.getValue().equals(m.getRole()) || MessageType.ASSISTANT.getValue().equals(m.getRole()))
                .map(m -> STR."\{m.getRole()}:\{m.getContent()}")
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

    /**
     * 调用 LLM之后
     *
     * @param chatClientResponse LLM响应
     * @param advisorChain       链
     * @return 处理后的LLM响应
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        // Get the scheduler from BaseAdvisor
        Scheduler scheduler = this.getScheduler();

        // Process the request with the before method
        return Mono.just(chatClientRequest)
                .publishOn(scheduler)
                .map(request -> this.before(request, streamAdvisorChain))
                .flatMapMany(streamAdvisorChain::nextStream)
                //汇总流式结果
                .transform(flux -> new ChatClientMessageAggregator().aggregateChatClientResponse(flux,
                        response -> this.after(response, streamAdvisorChain)));
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

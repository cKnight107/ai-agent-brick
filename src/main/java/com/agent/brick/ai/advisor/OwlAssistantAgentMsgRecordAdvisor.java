package com.agent.brick.ai.advisor;

import com.agent.brick.constants.AgentConstants;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.model.AiChatRecord;
import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;
import com.agent.brick.service.AiChatRecordService;
import com.agent.brick.util.SpringContextUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/7/15
 */
@Slf4j
public class OwlAssistantAgentMsgRecordAdvisor implements BaseAdvisor {

    private AiReq req;

    public OwlAssistantAgentMsgRecordAdvisor(AiReq aiReq) {
        this.req = aiReq;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        //添加记录
        Long chatId =req.getChatId();
        Long parentId = req.getParentId();
        //获取所有响应
        if (Objects.nonNull(chatClientResponse.chatResponse()) &&
                Objects.nonNull(chatClientResponse.chatResponse().getResult()) &&
                Objects.nonNull(chatClientResponse.chatResponse().getResult().getOutput())) {
            AssistantMessage output = chatClientResponse.chatResponse().getResult().getOutput();
            String text = output.getText();
            //获取上一次 记录id
            AiChatRecordService chatRecordService = SpringContextUtils.getBean(AiChatRecordService.class);
            AiChatRecord chatRecord = AiChatRecord.builder()
                    .name(AgentConstants.OWL_ASSISTANT_AGENT)
                    .chatId(chatId)
                    .parentId(parentId)
                    .build();
            chatRecord.setId(IdWorker.getId());
            //替换父id
            req.setParentId(chatRecord.getId());
            //record msg入库
            chatRecordService.insert(
                    chatRecord,
                    ChatRecordMsgJsonDto.builder()
                            .msg(text)
                            .build()
            );
        }
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
        return 0;
    }
}

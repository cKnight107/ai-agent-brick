package com.agent.brick.ai.advisor;

import com.agent.brick.ai.agent.TitleAgent;
import com.agent.brick.ai.agent.enums.AgentEnum;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.model.AiChat;
import com.agent.brick.model.AiChatRecord;
import com.agent.brick.pojo.dto.SysCacheUserDto;
import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;
import com.agent.brick.service.AiChatRecordService;
import com.agent.brick.service.AiChatService;
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
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Objects;

/**
 * <p>
 * 消息记录拦截器
 * </p>
 *
 * @author cKnight
 * @since 2025/7/7
 */
@Slf4j
public class MsgRecordAdvisor implements BaseAdvisor {

    /**
     * 聊天接口入参
     */
    private final AiReq req;

    private Long chatRecordId;

    public MsgRecordAdvisor(AiReq req) {
        this.req = req;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        Long chatId = req.getChatId();
        SysCacheUserDto sysCacheUserDto = req.getSysCacheUserDto();
        if (Objects.isNull(chatId) ||Objects.isNull(sysCacheUserDto)) {
            log.warn("消息记录拦截器，before，未获取到chatId或sysCacheUserDto，req:{}",req);
            return chatClientRequest;
        }
        Long userId = sysCacheUserDto.getId();
        AiChatService chatService = SpringContextUtils.getBean(AiChatService.class);
        //查看是否是第一次对话
        AiChat aiChat = chatService.queryChatById(chatId);
        String content = req.getMessage().getContent();
        Long parentId = null;
        if (Objects.isNull(aiChat)) {
            //第一次对话 则总结title
            TitleAgent titleAgent = AgentEnum.TITLE_AGENT.getAgent();
            String title = titleAgent
                    .chatClient()
                    .user(content)
                    .call()
                    .content();
            //chat 入库
            AiChat chat = AiChat.builder().title(title).userId(userId).build();
            chat.setId(chatId);
            chatService.insert(chat);
            parentId = 0L;
        }
        AiChatRecordService chatRecordService = SpringContextUtils.getBean(AiChatRecordService.class);
        if (Objects.isNull(parentId)){
            //为空则获取上一次char record id
            parentId = chatRecordService.queryNameLastParentId(chatId,sysCacheUserDto.getAccountNo());
        }
        this.chatRecordId = IdWorker.getId();
        //record和msg 入库
        AiChatRecord chatRecord = AiChatRecord.builder()
                .chatId(chatId)
                .name(sysCacheUserDto.getAccountNo())
                .parentId(parentId).build();
        chatRecord.setId(chatRecordId);
        chatRecordService.insert(
                chatRecord,
                ChatRecordMsgJsonDto.builder().msg(content).build()
        );
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        Long chatId = req.getChatId();
        //获取所有响应
        if (Objects.nonNull(chatClientResponse.chatResponse()) &&
                Objects.nonNull(chatClientResponse.chatResponse().getResult()) &&
                Objects.nonNull(chatClientResponse.chatResponse().getResult().getOutput())) {
            AssistantMessage output = chatClientResponse.chatResponse().getResult().getOutput();
            //获取上一次 记录id
            AiChatRecordService chatRecordService = SpringContextUtils.getBean(AiChatRecordService.class);
            //record msg入库
            chatRecordService.insert(
                    AiChatRecord.builder()
                            .name(GlobalConstants.ASSISTANT_AGENT_NAME)
                            .chatId(chatId)
                            .parentId(this.chatRecordId)
                            .build(),
                    ChatRecordMsgJsonDto.builder().msg(output.getText()).build()
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
        return 1;
    }
}

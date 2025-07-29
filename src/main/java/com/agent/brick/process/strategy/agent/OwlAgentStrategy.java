package com.agent.brick.process.strategy.agent;

import com.agent.brick.ai.agent.*;
import com.agent.brick.base.BaseDO;
import com.agent.brick.constants.AgentConstants;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.controller.request.AiMessageReq;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.enums.AiChatTypeEnum;
import com.agent.brick.mapper.AiChatMapper;
import com.agent.brick.model.AiChat;
import com.agent.brick.model.AiChatRecord;
import com.agent.brick.pojo.dto.AiChatRecordDto;
import com.agent.brick.pojo.dto.SysCacheUserDto;
import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;
import com.agent.brick.service.AiChatRecordService;
import com.agent.brick.util.AiUtil;
import com.agent.brick.util.CommonUtils;
import com.agent.brick.util.ConvertUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ThreadUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * OWL智能体策略
 * </p>
 * <a href="https://github.com/camel-ai/owl">github</a>
 *
 * @author cKnight
 * @since 2025/7/13
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OwlAgentStrategy extends AbstractAgentStrategy{
    private final OwlUserAgent owlUserAgent;

    private final OwlAssistantAgent owlAssistantAgent;

    private final OwlTaskSpecifiedAgent owlTaskSpecifiedAgent;

    private final OwlTaskPlannerAgent owlTaskPlannerAgent;

    private final TitleAgent titleAgent;

    private final AiChatRecordService aiChatRecordService;

    private final AiChatMapper aiChatMapper;

    /** 任务结束标识 */
    public static final String TASK_DONE = "TASK_DONE";

    /** 用户智能体开场语句 */
    public static final String USER_AGENT_INIT = "现在请逐步给我下达指令，以解决整个任务。如果该任务需要一些特定知识，请指示我使用工具来完成任务。";

    /** 助手Agent向用户Agent对话 */
    private static final String ASSISTANT_TO_USER = STR."""
            根据我的响应和我们当前的任务向我提供下一个指令和输入（如果需要）：{\{GlobalConstants.QUERY}}
            在给出最终答案之前，请检查我是否已经尽可能地使用不同的工具包，重新检查了最终答案。如果没有，请提醒我这样做。
            如果你觉得我们的任务已经完成了，回复"\{TASK_DONE}"来结束我们的对话。
            """;

    /** 用户Agent向助手Agent对话 */
    private static final String USER_TO_ASSISTANT = STR."""
            以下是关于整体任务的辅助信息，可能有助于您理解当前任务的意图：
            {\{GlobalConstants.QUERY}}
            如果有可用的工具，而且你想调用它们，千万不要说"我会……"，而是先调用，根据调用的结果做出回应，然后告诉我你调用了哪个工具。
            """;

    /** 用户Agent向助手Agent对话最终对话 */
    private static final String USER_TO_ASSISTANT_FINISH = STR."""
            现在请根据我们的对话对原任务做出最终回答：{\{GlobalConstants.QUERY}}
            """;

    /** 循环轮数 */
    private static final Integer RAND_MAX = 15;
    /** 任务最大字数 */
    private static final Integer WORD_LIMIT = 50;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object call(AiReq req) {
        CommonUtils.checkArgs(req.getChatId(),req.getMessage(),req.getSysCacheUserDto());
        AiMessageReq message = req.getMessage();
        String task = message.getContent();
        Long chatId = req.getChatId();
        log.info("owlAgent策略，chatId:{}，任务:{}",chatId,task);
        //添加用户对话记录 添加 parentId
        createChat(req);
        AiReq copyReq = ConvertUtils.beanProcess(req, AiReq.class);
        //获取历史对话信息
        copyReq.setAgentMessages(queryAgentMsgs(chatId));
        if (req.isTaskSpecifiedFlag()){
            //优化任务语句
            log.info("chatId:{}，开始优化task:{}",chatId,task);
            processTask(copyReq);
            task = copyReq.getMessage().getContent();
        }
        //开始对话
        String content = USER_AGENT_INIT;
        boolean taskDownFlag;
        for (Integer i = 0; i < RAND_MAX; i++) {
            if (StringUtils.isEmpty(content)){
                log.warn("chatId:{},owlAgent对话content为空:{}",chatId,req);
                break;
            }
            content = owlUserAgent.chatClient(copyReq).user(content).call().content();
            log.info("chatId:{},owlAgent第<{}>轮对话,userAgent响应:{}",chatId,i+1,content);

            //添加userAgent对话
            addAgentMsgs(copyReq.getAgentMessages(),GlobalConstants.USER_AGENT_NAME,content);

            taskDownFlag = content.contains(TASK_DONE);
            content = content + AiUtil.strFormat(Map.of(GlobalConstants.QUERY,task),(taskDownFlag ? USER_TO_ASSISTANT_FINISH:USER_TO_ASSISTANT));
            //减缓调用速率
            try {
                ThreadUtils.sleep(Duration.ofSeconds(1));
            } catch (InterruptedException e) {}
            content = owlAssistantAgent.chatClient(copyReq)
                    .user(content)
                    .call()
                    .content();
            log.info("chatId:{},owlAgent第<{}>轮对话,assistant结果:{}",chatId,i+1,content);

            //添加assistantAgent对话
            addAgentMsgs(copyReq.getAgentMessages(),GlobalConstants.ASSISTANT_AGENT_NAME,content);
            if (taskDownFlag){
                break;
            }else {
                content = content + AiUtil.strFormat(Map.of(GlobalConstants.QUERY,task),ASSISTANT_TO_USER);
            }
        }
        log.info("{}","=".repeat(30));
        log.info("chatId:{},owlAgent最终对话:{}",chatId,
                copyReq.getAgentMessages()
                        .stream()
                        .map(obj->STR."\{obj.getRole()}:\{obj.getContent()}")
                        .collect(Collectors.joining(System.lineSeparator())));
        return content;
    }

    private List<AiMessageReq> queryAgentMsgs(Long chatId) {
        //获取当前 chat 下的owl历史对话
        List<AiChatRecordDto> recordList = aiChatRecordService.queryListForSuper(chatId, AgentConstants.OWL_USER_AGENT);
        if (CollectionUtils.isEmpty(recordList)) {
            return new ArrayList<>();
        }
        return recordList.stream()
                .map(obj-> AiMessageReq.builder()
                                        .role(obj.getName())
                                        .content(obj.getMsgJsonDto().getMsg()).build())
                .collect(Collectors.toList());
    }

    private void createChat(AiReq req) {
        Long chatId = req.getChatId();
        String content = req.getMessage().getContent();
        SysCacheUserDto sysCacheUserDto = req.getSysCacheUserDto();
        Long count = where(aiChatMapper)
                .eq(BaseDO::getId, chatId)
                .count();
        if (count == 0) {
            //添加chat
            //总结标题
            String title = titleAgent.chatClient().user(content).call().content();
            AiChat aiChat = AiChat.builder()
                    .title(title)
                    .type(AiChatTypeEnum.OWL_TASK)
                    .userId(sysCacheUserDto.getId())
                    .build();
            aiChat.setId(chatId);
            aiChatMapper.insert(aiChat);
        }
        //添加record
        Long chatRecordId = IdWorker.getId();
        AiChatRecord chatRecord = AiChatRecord.builder()
                .name(sysCacheUserDto.getAccountNo())
                .chatId(chatId)
                .parentId(0L)
                .build();
        chatRecord.setId(chatRecordId);
        aiChatRecordService.insert(chatRecord, ChatRecordMsgJsonDto.builder().msg(content).build());
        req.setParentId(chatRecordId);
    }

    private void addAgentMsgs(List<AiMessageReq> agentMessages, String role, String content) {
        agentMessages.add(AiMessageReq.builder().role(role).content(content).build());
    }

    private void processTask(AiReq aiReq) {
        String task = "";
        if (Objects.isNull(aiReq.getWorkLimit())){
            aiReq.setWorkLimit(WORD_LIMIT);
        }
        task = owlTaskSpecifiedAgent.call(aiReq).content();
        log.info("owlTaskSpecifiedAgent结果:{}",task);
        aiReq.getMessage().setContent(task);
        task = owlTaskPlannerAgent.chatClient(aiReq).call().content();
        log.info("owlTaskPlannerAgent结果:{}",task);
        aiReq.getMessage().setContent(task);
    }
}

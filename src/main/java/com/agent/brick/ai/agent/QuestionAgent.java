package com.agent.brick.ai.agent;

import com.agent.brick.ai.advisor.AgentMemoryAdvisor;
import com.agent.brick.ai.advisor.AgentMsgRecordAdvisor;
import com.agent.brick.ai.prompt.annotation.PromptTool;
import com.agent.brick.ai.prompt.constants.PromptShotConstants;
import com.agent.brick.constants.AgentConstants;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.pojo.dto.AgentMsgDto;
import com.agent.brick.service.AiChatRecordService;
import com.agent.brick.util.AiUtil;
import com.agent.brick.util.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 出题师智能体
 * @since 2025/6/21
 * @author cKnight
 */
@Slf4j
public class QuestionAgent extends AbstractAgent{
    public static Builder<QuestionAgent> builder(){return new Builder<>(QuestionAgent.class);}

    @PromptTool(rules = {"**上下文感知**：在传递任务上下文参数中，你"+ PromptShotConstants.SHOULD+"优先传递所需要的知识点，知识点可能是检索的内容。"})
    @Tool(description = "根据任务出各科的题目包括试卷、练习题等")
    public String questionAgentCall(
            @ToolParam(description = "任务") String task,
            @ToolParam(description = "对话ID") Long chatId,
            @ToolParam(description = "任务上下文") String context,
            @ToolParam(description = "调用者名称",required = false) String callName
    ){
        log.info("调用questionAgentCall,任务:{},上下文:{},chatID:{},调用者:{}", task,context,chatId,callName);
        AiChatRecordService aiChatRecordService = SpringContextUtils.getBean(AiChatRecordService.class);
        AgentMsgDto agentMsgDto = AgentMsgDto.builder()
                .agentName(StringUtils.isEmpty(agentName) ? AgentConstants.QUESTION_AGENT_NAME : agentName)
                .chatId(chatId)
                .userName(StringUtils.isNotEmpty(callName) ? callName : GlobalConstants.ASSISTANT_AGENT_NAME)
                .query(task)
                .build();
        return this.chatClient()
                .messages(AiUtil.genAgentUserMessage(task,context))
                .advisors(
                        AgentMemoryAdvisor.builder().agentMsgDto(agentMsgDto).recordService(aiChatRecordService).build(),
                        AgentMsgRecordAdvisor.builder().agentMsgDto(agentMsgDto).recordService(aiChatRecordService).build()
                )
                .call()
                .content();
    }
}

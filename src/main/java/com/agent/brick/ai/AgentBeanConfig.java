package com.agent.brick.ai;

import com.agent.brick.ai.agent.*;
import com.agent.brick.ai.model.KimiChatModel;
import com.agent.brick.ai.model.QwenChatModel;
import com.agent.brick.ai.model.optins.QwenChatOptions;
import com.agent.brick.ai.prompt.AgentPromptConstants;
import com.agent.brick.ai.tools.RagTools;
import com.agent.brick.constants.AgentConstants;
import com.agent.brick.constants.ChatModelConstants;
import com.agent.brick.ai.model.enums.ChatModelEnum;
import com.agent.brick.enums.LLMEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * agent bean
 * @since 2025/6/21
 *
 * @author cKnight
 */
@Configuration
@Slf4j
public class AgentBeanConfig {

    @Bean(name = AgentConstants.QUESTION_AGENT_NAME)
    public QuestionAgent questionAgent(@Qualifier(ChatModelConstants.QWEN_3_30B_CHAT_MODEL) QwenChatModel qwen30bChatModel) {
        return QuestionAgent.builder()
                .agentName(AgentConstants.QUESTION_AGENT_NAME)
                .agentIntroduction("我是个一个出题师，有着丰富的出题经验，精通各个科目的出题技巧")
                .chatModel(qwen30bChatModel)
                .prompt(AgentPromptConstants.QUESTION_AGENT_SYSTEM_TEMPLATE)
                .build();
    }

//    @Bean(AgentConstants.OWL_USER_AGENT)
//    public OwlUserAgent owlUserAgent(@Qualifier("AiCommonApi qwenApi) {
//        return OwlUserAgent.builder().chatModel(
//                LLMEnum.QWEN.genChatModel(
//                        QwenChatOptions.builder()
//                                .model(ChatModelEnums.QWEN_3)
//                                .temperature(0.5)
//                                .maxTokens(3048)
//                                .build()
//                                .enableThinking(false),
//                        qwenApi
//                )
//        ).build();
//    }

    @Bean(AgentConstants.OWL_USER_AGENT)
    public OwlUserAgent owlUserAgent(KimiChatModel kimiK2ChatModel) {
        return OwlUserAgent.builder().chatModel(kimiK2ChatModel).build();
    }

    @Bean(AgentConstants.OWL_ASSISTANT_AGENT)
    public OwlAssistantAgent owlAssistantAgent(KimiChatModel kimiK2ChatModel) {
        return OwlAssistantAgent.builder().chatModel(kimiK2ChatModel).build();
    }

//    @Bean(AgentConstants.OWL_ASISTANT_AGENT)
//    public OwlAssistantAgent owlAssistantAgent(@Qualifier("AiCommonApi qwenApi) {
//        return OwlAssistantAgent.builder().chatModel(
//                LLMEnum.QWEN.genChatModel(
//                        QwenChatOptions.builder()
//                        .model(ChatModelEnums.QWEN_3)
//                        .temperature(0.5)
//                        .maxTokens(8000)
//                        .build()
//                        .enableThinking(false),
//                        qwenApi
//
//        )).build();
//    }

    @Bean(AgentConstants.OWL_TASK_SPECIFIED_AGENT)
    public OwlTaskSpecifiedAgent owlTaskSpecifiedAgent(@Qualifier("qwenApi") AiCommonApi qwenApi) {
        return OwlTaskSpecifiedAgent.builder()
                .chatModel(
                        LLMEnum.QWEN.genChatModel(
                                QwenChatOptions.builder()
                                        .model(ChatModelEnum.QWEN_3_30B)
                                        .temperature(0.85)
                                        .maxTokens(2024)
                                        .build()
                                        .enableThinking(false),
                                qwenApi
                        )
                )
                .build();
    }

    @Bean(AgentConstants.OWL_TASK_PLANNER_AGENT)
    public OwlTaskPlannerAgent owlTaskPlannerAgent(@Qualifier("qwenApi") AiCommonApi qwenApi) {
        return OwlTaskPlannerAgent.builder()
                .chatModel(
                        LLMEnum.QWEN.genChatModel(
                                QwenChatOptions.builder()
                                        .model(ChatModelEnum.QWEN_3_30B)
                                        .temperature(0.85)
                                        .maxTokens(2024)
                                        .build()
                                        .enableThinking(false),
                                qwenApi
                        )
                )
                .build();
    }

    @Bean(AgentConstants.TITLE_AGENT)
    public TitleAgent titleAgent(@Qualifier("qwenApi") AiCommonApi qwenApi){
        return TitleAgent.builder()
                .chatModel(
                        LLMEnum.QWEN.genChatModel(
                                QwenChatOptions.builder()
                                        .model(ChatModelEnum.QWEN_3_30B)
                                        .temperature(0.85)
                                        .maxTokens(1024)
                                        .build()
                                        .enableThinking(false),
                                qwenApi
                        )
                )
                .prompt(AgentPromptConstants.TITLE_AGENT_SYSTEM_TEMPLATE.create())
                .build();
    }

    @Bean(AgentConstants.RAG_AGENT)
    public RagAgent ragAgent(@Qualifier(ChatModelConstants.QWEN_3_PLUS_CHAT_MODEL) QwenChatModel qwenChatModel,RagTools ragTools){
        return  RagAgent.builder()
                .chatModel(qwenChatModel)
                .prompt(AgentPromptConstants.RAG_AGEMNT_SYSTEM_PROMPT)
                .tools(ragTools)
                .build();
    }
}

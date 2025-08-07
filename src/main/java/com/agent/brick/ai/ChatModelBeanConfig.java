package com.agent.brick.ai;

import com.agent.brick.ai.model.KimiChatModel;
import com.agent.brick.ai.model.MiniMaxChatModel;
import com.agent.brick.ai.model.QwenChatModel;
import com.agent.brick.ai.model.ZhiPuChatModel;
import com.agent.brick.ai.model.optins.KimiOptions;
import com.agent.brick.ai.model.optins.MiniMaxOptions;
import com.agent.brick.ai.model.optins.QwenChatOptions;
import com.agent.brick.ai.model.optins.ZhiPuOptions;
import com.agent.brick.constants.ChatModelConstants;
import com.agent.brick.ai.model.enums.ChatModelEnum;
import com.agent.brick.enums.LLMEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * chat model bean
 * </p>
 *
 * @author cKnight
 * @since 2025/7/13
 */
@Configuration
@Slf4j
public class ChatModelBeanConfig {

    @Bean(ChatModelConstants.QWEN_3_INSTRUCT_2507_CHAT_MODEL)
    public QwenChatModel qwen3Instruct2507ChatModel(@Qualifier("qwenApi")AiCommonApi qwenApi){
        return QwenChatModel.builder()
                .aiCommonApi(qwenApi)
                .options(
                        QwenChatOptions.builder()
                                .model(ChatModelEnum.QWEN_3_INSTRUCT_2507)
                                .temperature(0.75)
                                .maxTokens(8048)
                                .build()
                ).build();
    }

    @Bean(ChatModelConstants.QWEN_KIMI_K2_CHAT_MODEL)
    public QwenChatModel qwenKimiK2ChatModel(@Qualifier("qwenApi")AiCommonApi qwenApi){
        return QwenChatModel.builder()
                .aiCommonApi(qwenApi)
                .options(
                        QwenChatOptions.builder()
                                .model(ChatModelEnum.QWEN_KIMI_K2)
                                .temperature(0.75)
                                .maxTokens(8048)
                                .build()
                ).build();
    }

    @Bean(ChatModelConstants.QWEN_3_PLUS_CHAT_MODEL)
    public QwenChatModel qwen3PlusChatModel(@Qualifier("qwenApi")AiCommonApi qwenApi){
        return QwenChatModel.builder()
                .aiCommonApi(qwenApi)
                .options(
                        QwenChatOptions.builder()
                                .model(ChatModelEnum.QWEN_3_PLUS)
                                .temperature(0.75)
                                .maxTokens(8048)
                                .build()
                                .enableThinking(false)
                ).build();
    }

    @Bean(ChatModelConstants.QWEN_3_30B_CHAT_MODEL)
    public QwenChatModel qwen30bChatModel(@Qualifier("qwenApi")AiCommonApi qwenApi){
        return QwenChatModel.builder()
                .aiCommonApi(qwenApi)
                .options(
                        QwenChatOptions.builder()
                                .model(ChatModelEnum.QWEN_3_30B)
                                .temperature(0.5)
                                .maxTokens(10000)
                                .build()
                                .enableThinking(false)
                ).build();
    }

    @Bean(ChatModelConstants.QWEN_3_CHAT_MODEL)
    public QwenChatModel qwen3ChatModel(@Qualifier("qwenApi") AiCommonApi qwenApi){
        return QwenChatModel.builder()
                .aiCommonApi(qwenApi)
                .options(
                        QwenChatOptions.builder()
                                .model(ChatModelEnum.QWEN_3)
                                .temperature(0.75)
                                .maxTokens(6048)
                                .build()
                                .enableThinking(false)
                )
                .build();
    }

    @Bean(ChatModelConstants.ZHIPU_Z1_FLASH_CHAT_MODEL)
    public ZhiPuChatModel zhiPuZ1FlashChaModel(@Qualifier("zhiPuApi") AiCommonApi zhiPuApi){
        return ZhiPuChatModel.builder()
                .aiCommonApi(zhiPuApi)
                .options(
                        ZhiPuOptions.builder()
                                .model(ChatModelEnum.ZHIPU_Z1_FLASH)
                                .temperature(0.75)
                                .maxTokens(8000)
                                .build()
                )
                .build();
    }

    @Bean(ChatModelConstants.KIMI_K2_CHAT_MODEL)
    public KimiChatModel kimiK2ChatModel(@Qualifier("kimiApi") AiCommonApi kimiApi){
        return (KimiChatModel) LLMEnum.KIMI.genChatModel(
                KimiOptions.builder()
                        .model(ChatModelEnum.KIMI_K2)
                        .temperature(0.7)
                        .maxTokens(8048)
                        .build(),
                kimiApi
        );
    }

    @Bean(ChatModelConstants.MINIMAX_M1_CHAT_MODEL)
    public MiniMaxChatModel miniMaxM1ChatModel(@Qualifier("miniMaxApi") AiCommonApi miniMaxApi){
        return (MiniMaxChatModel) LLMEnum.MINIMAX.genChatModel(
                MiniMaxOptions.builder()
                        .model(ChatModelEnum.MINIMAX_M1)
                        .temperature(0.9)
                        .maxTokens(4048)
                        .build(),
                miniMaxApi
        );
    }
}

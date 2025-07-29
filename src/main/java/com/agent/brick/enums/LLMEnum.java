package com.agent.brick.enums;

import com.agent.brick.ai.AiCommonApi;
import com.agent.brick.ai.model.KimiChatModel;
import com.agent.brick.ai.model.MiniMaxChatModel;
import com.agent.brick.ai.model.QwenChatModel;
import com.agent.brick.ai.model.ZhiPuChatModel;
import com.agent.brick.ai.model.optins.AbstractChatOptions;
import org.springframework.ai.chat.model.ChatModel;

/**
 * <p>
 * LLM厂商
 * </p>
 *
 * @author cKnight
 * @since 2025/7/14
 */
public enum LLMEnum {
    QWEN,
    ZHIPU,
    KIMI,
    MINIMAX,
    ;

    public <T extends AbstractChatOptions> ChatModel genChatModel( T options, AiCommonApi aiCommonApi){
        switch (this){
            case QWEN -> {
                return QwenChatModel.builder().aiCommonApi(aiCommonApi).options(options).build();
            }
            case ZHIPU -> {
                return ZhiPuChatModel.builder().aiCommonApi(aiCommonApi).options(options).build();
            }
            case KIMI -> {
                return KimiChatModel.builder().aiCommonApi(aiCommonApi).options(options).build();
            }
            case MINIMAX -> {
                return MiniMaxChatModel.builder().aiCommonApi(aiCommonApi).options(options).build();
            }
            default -> {
                return null;
            }
        }
    }
}

package com.agent.brick.enums;

import com.agent.brick.ai.model.AbstractChatModel;
import com.agent.brick.constants.ChatModelConstants;
import com.agent.brick.util.SpringContextUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @since 2025/6/3
 * @author cKnight
 */
@AllArgsConstructor
public enum ChatModelEnum {
    QWEN_3_PLUS("qwen-plus", LLMEnum.QWEN,ChatModelConstants.QWEN_3_PLUS_CHAT_MODEL),
    QWEN_3("qwen3-235b-a22b",LLMEnum.QWEN,ChatModelConstants.QWEN_3_CHAT_MODEL),
    QWEN_3_30B("qwen3-30b-a3b",LLMEnum.QWEN,null),
    QWEN_TEXT_EMBEDDING_4("text-embedding-v4",LLMEnum.QWEN,null),
    ZHIPU_Z1_FLASH("glm-z1-flash",LLMEnum.ZHIPU, ChatModelConstants.ZHIPU_Z1_FLASH_CHAT_MODEL),
    KIMI_K2("kimi-k2-0711-preview",LLMEnum.KIMI, ChatModelConstants.KIMI_K2_CHAT_MODEL),
    MINIMAX_M1("MiniMax-M1",LLMEnum.MINIMAX, ChatModelConstants.MINIMAX_M1_CHAT_MODEL),
    QWEN_KIMI_K2("Moonshot-Kimi-K2-Instruct",LLMEnum.QWEN, ChatModelConstants.QWEN_KIMI_K2_CHAT_MODEL),
    QWEN_3_INSTRUCT_2507("qwen3-235b-a22b-instruct-2507",LLMEnum.QWEN, ChatModelConstants.QWEN_3_INSTRUCT_2507_CHAT_MODEL),
    ;
    public final String value;
    public final LLMEnum LLM;
    public final String beanName;

    public <T extends AbstractChatModel> T getChatModel(){
        if (StringUtils.isEmpty(this.beanName)){
            return null;
        }
        return (T)SpringContextUtils.getBean(beanName);
    }
}

package com.agent.brick.ai.model.optins;

import com.agent.brick.util.ConvertUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @since 2025/6/4
 * @author cKnight
 */
@Data
public class QwenChatOptions extends AbstractChatOptions {

    /**
     * 是否深度思考
     */
    private @JsonProperty("enable_thinking") boolean enableThinking;

    @Override
    public AbstractChatOptions copy() {
        return ConvertUtils.beanProcess(this, QwenChatOptions.class);
    }


    public QwenChatOptions enableThinking(boolean enableThinking){
        this.enableThinking = enableThinking;
        return this;
    }

    public static Builder<QwenChatOptions> builder() {
        return new Builder<>(QwenChatOptions.class);
    }
}

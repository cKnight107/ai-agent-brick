package com.agent.brick.ai.model;

import com.agent.brick.ai.model.optins.AbstractChatOptions;
import com.agent.brick.ai.model.optins.QwenChatOptions;
import com.agent.brick.util.CommonUtils;
import com.agent.brick.util.ConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

/**
 * 千问 模型
 * @since 2025/6/4
 * @author cKnight
 */
@Slf4j
public class QwenChatModel extends AbstractChatModel {

    @Override
    protected AbstractChatOptions convertOption(Prompt prompt) {
        QwenChatOptions runtimeOptions = null;
        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
                runtimeOptions = ModelOptionsUtils.copyToTarget(toolCallingChatOptions, ToolCallingChatOptions.class,
                        QwenChatOptions.class);
            }
            else {
                runtimeOptions = ModelOptionsUtils.copyToTarget(prompt.getOptions(), ChatOptions.class,
                        QwenChatOptions.class);
            }
        }
        return runtimeOptions;
    }

    @Override
    protected AbstractChatOptions mergeOptions(AbstractChatOptions runtimeOptions) {
        return ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions,
                QwenChatOptions.class, CommonUtils.getAllFieldName(QwenChatOptions.class));
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return ConvertUtils.beanProcess(this.defaultOptions,QwenChatOptions.class);
    }

    public static Builder<QwenChatModel> builder(){
        return new Builder<>(QwenChatModel.class);
    }
}

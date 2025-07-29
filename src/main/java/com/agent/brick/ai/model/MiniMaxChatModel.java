package com.agent.brick.ai.model;

import com.agent.brick.ai.model.optins.AbstractChatOptions;
import com.agent.brick.ai.model.optins.MiniMaxOptions;
import com.agent.brick.util.CommonUtils;
import com.agent.brick.util.ConvertUtils;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/7/16
 */
public class MiniMaxChatModel extends AbstractChatModel{
    public static Builder<MiniMaxChatModel> builder(){return new Builder<>(MiniMaxChatModel.class);}

    @Override
    protected AbstractChatOptions convertOption(Prompt prompt) {
        MiniMaxOptions runtimeOptions = null;
        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
                runtimeOptions = ModelOptionsUtils.copyToTarget(toolCallingChatOptions, ToolCallingChatOptions.class,
                        MiniMaxOptions.class);
            }
            else {
                runtimeOptions = ModelOptionsUtils.copyToTarget(prompt.getOptions(), ChatOptions.class,
                        MiniMaxOptions.class);
            }
        }
        return runtimeOptions;
    }

    @Override
    protected AbstractChatOptions mergeOptions(AbstractChatOptions runtimeOptions) {
        return ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions,
                MiniMaxOptions.class, CommonUtils.getAllFieldName(MiniMaxOptions.class));
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return ConvertUtils.beanProcess(this.defaultOptions,MiniMaxOptions.class);
    }
}

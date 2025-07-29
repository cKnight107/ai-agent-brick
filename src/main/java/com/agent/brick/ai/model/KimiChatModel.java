package com.agent.brick.ai.model;

import com.agent.brick.ai.model.optins.AbstractChatOptions;
import com.agent.brick.ai.model.optins.KimiOptions;
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
public class KimiChatModel extends AbstractChatModel{
    public static Builder<KimiChatModel> builder(){return new Builder<>(KimiChatModel.class);}


    @Override
    protected AbstractChatOptions convertOption(Prompt prompt) {
        KimiOptions runtimeOptions = null;
        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
                runtimeOptions = ModelOptionsUtils.copyToTarget(toolCallingChatOptions, ToolCallingChatOptions.class,
                        KimiOptions.class);
            }
            else {
                runtimeOptions = ModelOptionsUtils.copyToTarget(prompt.getOptions(), ChatOptions.class,
                        KimiOptions.class);
            }
        }
        return runtimeOptions;
    }

    @Override
    protected AbstractChatOptions mergeOptions(AbstractChatOptions runtimeOptions) {
        return ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions,
                KimiOptions.class, CommonUtils.getAllFieldName(KimiOptions.class));
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return ConvertUtils.beanProcess(this.defaultOptions,KimiOptions.class);
    }
}

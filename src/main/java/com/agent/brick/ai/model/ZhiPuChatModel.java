package com.agent.brick.ai.model;

import com.agent.brick.ai.model.optins.AbstractChatOptions;
import com.agent.brick.ai.model.optins.ZhiPuOptions;
import com.agent.brick.util.CommonUtils;
import com.agent.brick.util.ConvertUtils;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

/**
 * <p>
 * 智普模型
 * </p>
 *
 * @author cKnight
 * @since 2025/7/16
 */
public class ZhiPuChatModel extends AbstractChatModel{

    public static Builder<ZhiPuChatModel> builder(){return new Builder<>(ZhiPuChatModel.class);}

    @Override
    protected AbstractChatOptions convertOption(Prompt prompt) {
        ZhiPuOptions runtimeOptions = null;
        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
                runtimeOptions = ModelOptionsUtils.copyToTarget(toolCallingChatOptions, ToolCallingChatOptions.class,
                        ZhiPuOptions.class);
            }
            else {
                runtimeOptions = ModelOptionsUtils.copyToTarget(prompt.getOptions(), ChatOptions.class,
                        ZhiPuOptions.class);
            }
        }
        return runtimeOptions;
    }

    @Override
    protected AbstractChatOptions mergeOptions(AbstractChatOptions runtimeOptions) {
        return ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions,
                ZhiPuOptions.class, CommonUtils.getAllFieldName(ZhiPuOptions.class));
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return ConvertUtils.beanProcess(this.defaultOptions,ZhiPuOptions.class);
    }
}

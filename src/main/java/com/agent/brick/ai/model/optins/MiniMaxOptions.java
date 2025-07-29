package com.agent.brick.ai.model.optins;

import com.agent.brick.util.ConvertUtils;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/7/16
 */
public class MiniMaxOptions extends AbstractChatOptions {
    public static Builder<MiniMaxOptions> builder() {return new Builder<>(MiniMaxOptions.class);}
    @Override
    public AbstractChatOptions copy() {
        return ConvertUtils.beanProcess(this, MiniMaxOptions.class);
    }
}

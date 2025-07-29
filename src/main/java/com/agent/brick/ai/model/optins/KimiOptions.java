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
public class KimiOptions extends AbstractChatOptions{
    public static Builder<KimiOptions> builder(){return new Builder<>(KimiOptions.class);}
    @Override
    public AbstractChatOptions copy() {
        return ConvertUtils.beanProcess(this,KimiOptions.class);
    }
}

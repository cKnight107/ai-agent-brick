package com.agent.brick.ai.model.optins;

import com.agent.brick.util.ConvertUtils;

/**
 * <p>
 * 智普配置
 * </p>
 *
 * @author cKnight
 * @since 2025/7/16
 */
public class ZhiPuOptions extends AbstractChatOptions{
    @Override
    public AbstractChatOptions copy() {
        return ConvertUtils.beanProcess(this, ZhiPuOptions.class);
    }

    public static Builder<ZhiPuOptions> builder(){return new Builder<>(ZhiPuOptions.class);}
}

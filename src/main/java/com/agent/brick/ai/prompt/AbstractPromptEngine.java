package com.agent.brick.ai.prompt;

import org.springframework.ai.chat.prompt.Prompt;

/**
 * <p>
 * prompt引擎抽象父类
 * </p>
 *
 * @author cKnight
 * @since 2025/8/6
 */
public abstract class AbstractPromptEngine implements PromptEngine{

    @Override
    public Prompt toPrompt() {
        return null;
    }

    @Override
    public String toMarkdown() {
        return null;
    }
}

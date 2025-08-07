package com.agent.brick.ai.prompt;

import org.springframework.ai.chat.prompt.Prompt;

/**
 * <p>
 * 提示词引擎
 * </p>
 *
 * @author cKnight
 * @since 2025/8/6
 */
public interface PromptEngine {
    /**
     * 转换为prompt
     * @return prompt
     */
    Prompt toPrompt();

    /**
     * 转换为markdown
     * @return
     */
    String toMarkdown();
}

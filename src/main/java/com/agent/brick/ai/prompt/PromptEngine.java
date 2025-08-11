package com.agent.brick.ai.prompt;

import org.springframework.ai.chat.prompt.Prompt;

import java.util.Map;

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
     * 转换为Prompt
     * @return prompt
     */
    Prompt toPrompt();

    Prompt toPrompt(Map<String,Object> model);

    /**
     * 转换为markdown
     * @return md string
     */
    String toMarkdown();
}

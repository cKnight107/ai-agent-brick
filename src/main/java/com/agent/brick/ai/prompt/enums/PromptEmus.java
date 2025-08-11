package com.agent.brick.ai.prompt.enums;

import com.agent.brick.ai.prompt.AbstractPromptEngine;
import com.agent.brick.ai.prompt.WwhPromptEngine;
import com.agent.brick.ai.prompt.constants.PromptBeanConstants;
import com.agent.brick.util.SpringContextUtils;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 提示词池
 * </p>
 *
 * @author cKnight
 * @since 2025/8/11
 */
@AllArgsConstructor
public enum PromptEmus {
    ASSISTANT_TER_WWH_PROMPT(PromptBeanConstants.ASSISTANT_TEACHER_WWH_PROMPT, WwhPromptEngine.class)
    ;

    public <T extends AbstractPromptEngine> T getPrompt(){
        return (T) SpringContextUtils.getBean(this.name, this.clazz);
    }

    public final String name;
    public final Class<? extends AbstractPromptEngine> clazz;
}

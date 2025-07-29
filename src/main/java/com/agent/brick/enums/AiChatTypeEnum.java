package com.agent.brick.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;

/**
 * <p>
 * aiChat类型枚举
 * </p>
 *
 * @author cKnight
 * @since 2025/7/15
 */
@AllArgsConstructor
public enum AiChatTypeEnum implements IEnum<String> {
    CHAT,
    OWL_TASK
    ;

    @Override
    public String getValue() {
        return this.name();
    }
}

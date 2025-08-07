package com.agent.brick.ai.prompt.enums;

import lombok.AllArgsConstructor;

/**
 * <p>
 * 输入源枚举
 * </p>
 *
 * @author cKnight
 * @since 2025/8/7
 */
@AllArgsConstructor
public enum InputSourceEnum {
    USER_QUERY("<userQuery>","用户的直接提问"),
    CHAT_HISTORY("<chatHistory>","上下文对话记录"),
    USER_INFO("<userInfo>","用户的信息")
    ;
    /** xml标签 */
    public String xmlTag;
    /** 介绍 */
    public String description;

}

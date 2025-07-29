package com.agent.brick.pojo.json.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 对话记录消息json 实体类
 * </p>
 * @see com.agent.brick.model.AiChatRecordMsg msgDetail
 * @author cKnight
 * @since 2025/7/7
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRecordMsgJsonDto {
    private String msg;
}

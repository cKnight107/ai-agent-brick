package com.agent.brick.pojo.dto;

import com.agent.brick.pojo.json.db.ChatRecordMsgJsonDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 对话记录传输类
 * </p>
 *
 * @author cKnight
 * @since 2025/7/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatRecordDto {
    private String name;

    private ChatRecordMsgJsonDto msgJsonDto;
}

package com.agent.brick.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 智能体消息传输类
 * </p>
 *
 * @author cKnight
 * @since 2025/7/7
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AgentMsgDto {
    private Long chatId;

    private String query;

    private String userName;

    private String agentName;
}

package com.agent.brick.controller.request;

import com.agent.brick.pojo.dto.SysCacheUserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @since 2025/6/2
 *
 * @author cKnight
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiReq {
    /** 是否开启深度思考 */
    private boolean thinkFlag = false;

    /** 用户id */
    private Long userId;

    /** 对话id */
    private Long chatId;

    /** 当前消息 */
    private AiMessageReq message;

    /** 历史消息 */
    private List<AiMessageReq> messages;

    private List<AiMessageReq> agentMessages;

    private SysCacheUserDto  sysCacheUserDto;

    /** 是否优化任务语句 */
    private boolean taskSpecifiedFlag = false;

    /** 字数限制 */
    private Integer workLimit;

    private Long parentId;


    @Override
    public String toString() {
        return STR."chatId(当前对话ID)=\{chatId}, message(用户的问题或任务)=\{message.getContent()}";
    }
}

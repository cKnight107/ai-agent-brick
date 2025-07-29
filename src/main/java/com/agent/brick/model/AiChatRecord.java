package com.agent.brick.model;

import com.agent.brick.base.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;


/**
 * <p>
 * 
 * </p>
 *
 * @author autoCode
 * @since 2025-07-05
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("ai_chat_record")
@Accessors(chain = true)
public class AiChatRecord extends BaseDO {

    /**
     * 对话ID
     */
    @TableField("chat_id")
    private Long chatId;

    /**
     * 父ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 消息ID
     */
    @TableField("msg_id")
    private Long msgId;

    /**
     * 对话主体名称
     */
    @TableField("name")
    private String name;
}

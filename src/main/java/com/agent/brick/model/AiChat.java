package com.agent.brick.model;

import com.agent.brick.base.BaseDO;
import com.agent.brick.enums.AiChatTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;


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
@TableName("ai_chat")
public class AiChat extends BaseDO {

    /**
     * 用戶id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    @TableField(value = "type")
    private AiChatTypeEnum type;
}

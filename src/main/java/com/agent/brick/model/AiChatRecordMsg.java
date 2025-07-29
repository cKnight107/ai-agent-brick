package com.agent.brick.model;

import com.agent.brick.base.BaseDO;
import com.agent.brick.base.db.JsonbTypeHandler;
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
@TableName("ai_chat_record_msg")
public class AiChatRecordMsg extends BaseDO {

    /**
     * 消息详情内容JSON
     */
    @TableField(value = "msg_detail",typeHandler = JsonbTypeHandler.class)
    private Object msgDetail;
}

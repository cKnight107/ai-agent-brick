package com.agent.brick.pojo.vo;

import com.agent.brick.base.BaseVO;
import com.agent.brick.enums.AiChatTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/7/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiChatVO extends BaseVO {
    /**
     * 标题
     */
    private String title;

    private AiChatTypeEnum type;

    /**
     * 创建时间 数据库层面添加
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}

package com.agent.brick.pojo.vo;

import com.agent.brick.base.BaseVO;
import com.agent.brick.enums.RoleCodeEnum;
import lombok.*;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/7/4
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiUserVO extends BaseVO {
    /**
     * 账号
     */
    private String accountNo;

    /**
     * 昵称
     */
    private String name;

    /**
     * 令牌
     */
    private String token;

    /**
     * 用户角色
     */
    private List<RoleCodeEnum> roleList;
}

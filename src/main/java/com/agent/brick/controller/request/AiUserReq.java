package com.agent.brick.controller.request;

import com.agent.brick.base.BaseQueryDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
public class AiUserReq extends BaseQueryDto {
    /**
     * 账号
     */
    private String accountNo;

    /**
     * 昵称
     */
    private String name;

    /**
     * 密码
     */
    private String password;

    /**
     * 角色ids
     */
    private List<Long> roleIds;

}

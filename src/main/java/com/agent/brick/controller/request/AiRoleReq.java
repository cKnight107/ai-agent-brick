package com.agent.brick.controller.request;

import com.agent.brick.base.BaseQueryDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
public class AiRoleReq extends BaseQueryDto {
    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;
}

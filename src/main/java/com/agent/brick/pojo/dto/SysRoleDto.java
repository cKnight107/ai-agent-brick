package com.agent.brick.pojo.dto;

import lombok.Data;

/**
 * 角色信息
 * @author cKnight
 * @since 2024/12/18
 */
@Data
public class SysRoleDto {
    private Long id;
    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限字符串
     */
    private String roleKey;

    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
     */
    private String dataScope;}

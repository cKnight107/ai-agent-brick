package com.agent.brick.service;

import com.agent.brick.model.AiRole;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author autoCode
 * @since 2025-07-04
 */
public interface AiRoleService{

    /**
     * 根据ids获取角色
     * @param ids ids
     * @return role list
     */
    List<AiRole> queryListByIds(List<Long> ids);
}

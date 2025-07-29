package com.agent.brick.service;

import com.agent.brick.model.AiUserRoleRel;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author autoCode
 * @since 2025-07-04
 */
public interface AiUserRoleRelService{

    /**
     * 创建用户角色关联
     * @param userId 用户id
     * @param roleIds 角色ids
     */
    void createUserRole(Long userId, List<Long> roleIds);

    /**
     * 获取用户角色关联信息
     * @param userId
     * @return
     */
    List<AiUserRoleRel> queryListByUserId(Long userId);
}

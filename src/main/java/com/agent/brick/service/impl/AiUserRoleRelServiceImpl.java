package com.agent.brick.service.impl;

import com.agent.brick.base.BaseService;
import com.agent.brick.mapper.AiUserRoleRelMapper;
import com.agent.brick.model.AiUserRoleRel;
import com.agent.brick.service.AiUserRoleRelService;
import com.agent.brick.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author autoCode
 * @since 2025-07-04
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiUserRoleRelServiceImpl extends BaseService implements AiUserRoleRelService {

    private final AiUserRoleRelMapper roleRelMapper;

    @Override
    public void createUserRole(Long userId, List<Long> roleIds) {
        CommonUtils.checkArgs(userId,roleIds);
        List<AiUserRoleRel> list = roleIds
                .stream()
                .map(obj -> AiUserRoleRel.builder().roleId(obj).userId(userId).build())
                .toList();
        insertBatch(list,AiUserRoleRelMapper.class);
    }

    @Override
    public List<AiUserRoleRel> queryListByUserId(Long userId) {
        return where(roleRelMapper)
                .eq(AiUserRoleRel::getUserId,userId)
                .list();
    }
}

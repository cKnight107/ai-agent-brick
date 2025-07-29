package com.agent.brick.service.impl;

import com.agent.brick.base.BaseDO;
import com.agent.brick.base.BaseService;
import com.agent.brick.mapper.AiRoleMapper;
import com.agent.brick.model.AiRole;
import com.agent.brick.service.AiRoleService;
import com.agent.brick.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author autoCode
 * @since 2025-07-04
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiRoleServiceImpl extends BaseService implements AiRoleService {
    private  final AiRoleMapper aiRoleMapper;

    @Override
    public List<AiRole> queryListByIds(List<Long> ids) {
        CommonUtils.checkArgs(ids);
        return where(aiRoleMapper).in(BaseDO::getId,ids).list();
    }
}

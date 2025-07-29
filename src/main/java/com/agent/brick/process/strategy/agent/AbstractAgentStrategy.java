package com.agent.brick.process.strategy.agent;

import com.agent.brick.base.BaseService;
import com.agent.brick.controller.request.AiReq;

/**
 * <p>
 * 智能体策略 抽象父类
 * </p>
 *
 * @author cKnight
 * @since 2025/7/13
 */
public abstract class AbstractAgentStrategy extends BaseService implements AgentStrategy{

    @Override
    public Object call(AiReq req) {return null;}
}

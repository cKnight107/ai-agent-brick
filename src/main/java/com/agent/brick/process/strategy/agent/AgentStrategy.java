package com.agent.brick.process.strategy.agent;

import com.agent.brick.controller.request.AiReq;

/**
 * <p>
 * 智能体策略
 * </p>
 *
 * @author cKnight
 * @since 2025/7/13
 */
public interface AgentStrategy {

    /**
     * 调用
     * @param req 入参
     * @return obj
     */
    Object call(AiReq req);
}

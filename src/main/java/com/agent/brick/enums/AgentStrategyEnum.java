package com.agent.brick.enums;

import com.agent.brick.process.strategy.agent.AgentStrategy;
import com.agent.brick.process.strategy.agent.OwlAgentStrategy;
import lombok.AllArgsConstructor;

/**
 * <p>
 * 智能体策略工厂
 * </p>
 *
 * @author cKnight
 * @since 2025/7/13
 */
@AllArgsConstructor
public enum AgentStrategyEnum {
    OWL(OwlAgentStrategy.class)
    ;
    private final Class<? extends AgentStrategy> clazz;
}

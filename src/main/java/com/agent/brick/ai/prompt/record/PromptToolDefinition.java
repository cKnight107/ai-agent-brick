package com.agent.brick.ai.prompt.record;

import java.util.List;

/**
 * <p>
 * prompt 工具定义
 * </p>
 *
 * @param name 工具名称
 * @param description 工具介绍
 * @param rules 规则列表
 * @author cKnight
 * @since 2025/8/7
 */
public record PromptToolDefinition(String name,String description,String inputSchema,List<Parameters> parameters, List<String> rules) {
}
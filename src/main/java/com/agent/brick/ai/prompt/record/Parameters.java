package com.agent.brick.ai.prompt.record;

/**
 * <p>
 * 方法参数
 * </p>
 * @param name 参数名称
 * @param description 参数介绍
 * @author cKnight
 * @since 2025/8/8
 */
public record Parameters(String name, String description, boolean required) {
}

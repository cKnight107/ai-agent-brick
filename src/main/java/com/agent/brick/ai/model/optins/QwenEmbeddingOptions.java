package com.agent.brick.ai.model.optins;

/**
 * @since 2025/6/27
 *
 * @author cKnight
 */
public class QwenEmbeddingOptions extends AbstractEmbeddingOptions{
    public static Builder<QwenEmbeddingOptions> builder(){
        return new Builder<>(QwenEmbeddingOptions.class);
    }
}

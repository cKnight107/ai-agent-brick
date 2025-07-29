package com.agent.brick.ai.model;

import com.agent.brick.ai.model.optins.AbstractEmbeddingOptions;
import com.agent.brick.ai.model.optins.QwenEmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.model.ModelOptionsUtils;

/**
 * @since 2025/6/27
 *
 * @author cKnight
 */
public class QwenEmbeddingModel extends AbstractMyEmbeddingModel{

    public static Builder<QwenEmbeddingModel> builder(){
        return new Builder<>(QwenEmbeddingModel.class);
    }

    @Override
    public AbstractEmbeddingOptions copyOptions(EmbeddingOptions options) {
        return ModelOptionsUtils.copyToTarget(options, EmbeddingOptions.class,
                QwenEmbeddingOptions.class);
    }

    @Override
    public AbstractEmbeddingOptions copyRequestOptions(AbstractEmbeddingOptions runtimeOptions) {
        return QwenEmbeddingOptions
                .builder()
                // Handle portable embedding options
                .model(ModelOptionsUtils.mergeOption(runtimeOptions.getModel(), this.defaultOptions.getModel()))
                .dimensions(
                        ModelOptionsUtils.mergeOption(runtimeOptions.getDimensions(), this.defaultOptions.getDimensions()))
                // Handle OpenAI specific embedding options
                .encodingFormat(ModelOptionsUtils.mergeOption(runtimeOptions.getEncodingFormat(),
                        this.defaultOptions.getEncodingFormat()))
                .user(ModelOptionsUtils.mergeOption(runtimeOptions.getUser(), this.defaultOptions.getUser()))
                .build();
    }
}

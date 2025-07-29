package com.agent.brick.ai.model;

import com.agent.brick.ai.AiCommonApi;
import com.agent.brick.ai.model.optins.AbstractEmbeddingOptions;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.*;
import org.springframework.ai.embedding.observation.DefaultEmbeddingModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationContext;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationDocumentation;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

/**
 * 嵌入模型抽象类
 * @see org.springframework.ai.openai.OpenAiEmbeddingModel
 * @author OpenAiEmbeddingModel
 */
@Slf4j
public abstract class AbstractMyEmbeddingModel extends AbstractEmbeddingModel {
    private static final EmbeddingModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultEmbeddingModelObservationConvention();

    protected EmbeddingModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

    protected MetadataMode metadataMode = MetadataMode.EMBED;

    protected RetryTemplate retryTemplate = RetryUtils.DEFAULT_RETRY_TEMPLATE;

    /**
     * Observation registry used for instrumentation.
     */
    protected ObservationRegistry observationRegistry = ObservationRegistry.NOOP;

    protected AbstractEmbeddingOptions defaultOptions;

    protected AiCommonApi aiCommonApi;


    @Override
    public float[] embed(Document document) {
        Assert.notNull(document, "Document must not be null");
        return this.embed(document.getFormattedContent(this.metadataMode));
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        // Before moving any further, build the final request EmbeddingRequest,
        // merging runtime and default options.
        EmbeddingRequest embeddingRequest = buildEmbeddingRequest(request);

        AiCommonApi.EmbeddingRequest<List<String>> apiRequest = createRequest(embeddingRequest);

        var observationContext = EmbeddingModelObservationContext.builder()
                .embeddingRequest(embeddingRequest)
                .provider(OpenAiApiConstants.PROVIDER_NAME)
                .build();

        return Objects.requireNonNull(EmbeddingModelObservationDocumentation.EMBEDDING_MODEL_OPERATION
                .observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                        this.observationRegistry)
                .observe(() -> {
                    AiCommonApi.EmbeddingList<AiCommonApi.Embedding> apiEmbeddingResponse = this.retryTemplate
                            .execute(ctx -> this.aiCommonApi.embeddings(apiRequest).getBody());

                    if (apiEmbeddingResponse == null) {
                        log.warn("No embeddings returned for request: {}", request);
                        return new EmbeddingResponse(List.of());
                    }

                    AiCommonApi.Usage usage = apiEmbeddingResponse.usage();
                    Usage embeddingResponseUsage = usage != null ? getDefaultUsage(usage) : new EmptyUsage();
                    var metadata = new EmbeddingResponseMetadata(apiEmbeddingResponse.model(), embeddingResponseUsage);

                    List<Embedding> embeddings = apiEmbeddingResponse.data()
                            .stream()
                            .map(e -> new Embedding(e.embedding(), e.index()))
                            .toList();

                    EmbeddingResponse embeddingResponse = new EmbeddingResponse(embeddings, metadata);

                    observationContext.setResponse(embeddingResponse);

                    return embeddingResponse;
                }));
    }

    private DefaultUsage getDefaultUsage(AiCommonApi.Usage usage) {
        return new DefaultUsage(usage.promptTokens(), usage.completionTokens(), usage.totalTokens(), usage);
    }


    private AiCommonApi.EmbeddingRequest<List<String>> createRequest(EmbeddingRequest request) {
        AbstractEmbeddingOptions requestOptions = (AbstractEmbeddingOptions) request.getOptions();
        return new AiCommonApi.EmbeddingRequest<>(request.getInstructions(), requestOptions.getModel(),
                requestOptions.getEncodingFormat(), requestOptions.getDimensions(), requestOptions.getUser());
    }


    public abstract AbstractEmbeddingOptions copyOptions(EmbeddingOptions options);

    public abstract AbstractEmbeddingOptions copyRequestOptions(AbstractEmbeddingOptions runtimeOptions);

    private EmbeddingRequest buildEmbeddingRequest(EmbeddingRequest embeddingRequest) {
        // Process runtime options
        AbstractEmbeddingOptions runtimeOptions = null;
        if (embeddingRequest.getOptions() != null) {
            runtimeOptions = copyOptions(embeddingRequest.getOptions());
        }

        AbstractEmbeddingOptions requestOptions = runtimeOptions == null ? this.defaultOptions : copyRequestOptions(runtimeOptions);

        return new EmbeddingRequest(embeddingRequest.getInstructions(), requestOptions);
    }

    /**
     * Use the provided convention for reporting observation data
     * @param observationConvention The provided convention
     */
    public void setObservationConvention(EmbeddingModelObservationConvention observationConvention) {
        Assert.notNull(observationConvention, "observationConvention cannot be null");
        this.observationConvention = observationConvention;
    }

    public static class Builder<T extends AbstractMyEmbeddingModel> {
        private T model;

        public Builder(Class<T> clazz){
            try {
                this.model = clazz.getDeclaredConstructor().newInstance();
            }catch (Exception e){}
        }

        public Builder<T> observationConvention(EmbeddingModelObservationConvention observationConvention) {
            this.model.observationConvention = observationConvention;
            return this;
        }

        public Builder<T> observationConvention(MetadataMode metadataMode) {
            this.model.metadataMode = metadataMode;
            return this;
        }

        public Builder<T> retryTemplate(RetryTemplate retryTemplate) {
            this.model.retryTemplate = retryTemplate;
            return this;
        }

        public Builder<T> observationRegistry(ObservationRegistry observationRegistry) {
            this.model.observationRegistry = observationRegistry;
            return this;
        }

        public Builder<T> defaultOptions(AbstractEmbeddingOptions defaultOptions) {
            this.model.defaultOptions = defaultOptions;
            return this;
        }

        public Builder<T> aiCommonApi(AiCommonApi aiCommonApi) {
            this.model.aiCommonApi = aiCommonApi;
            return this;
        }

        public T build() {
            return model;
        }
    }
}

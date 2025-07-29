package com.agent.brick.config;

import com.agent.brick.ai.AiCommonApi;
import com.agent.brick.ai.model.QwenChatModel;
import com.agent.brick.ai.model.QwenEmbeddingModel;
import com.agent.brick.ai.model.optins.QwenChatOptions;
import com.agent.brick.ai.model.optins.QwenEmbeddingOptions;
import com.agent.brick.ai.transformer.KeywordTransformer;
import com.agent.brick.enums.ChatModelEnum;
import com.knuddels.jtokkit.api.EncodingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/**
 * @since 2025/6/3
 * @author cKnight
 */
@Configuration
@Slf4j
public class BeanConfig {

    @Bean("qwenApi")
    public AiCommonApi qwenApi(QwenConfig qwenConfig, RestTemplate restTemplate){
        return AiCommonApi.builder()
                .apiKey(qwenConfig.getApiKey())
                .baseUrl(qwenConfig.getBaseUrl())
                .restClientBuilder(RestClient.builder(restTemplate))
                .build();
    }

    @Bean("zhiPuApi")
    public AiCommonApi zhiPuApi(ZhiPuConfig zhiPuConfig,RestTemplate restTemplate){
        return AiCommonApi.builder()
                .apiKey(zhiPuConfig.getApiKey())
                .baseUrl(zhiPuConfig.getBaseUrl())
                .completionsPath("/v4/chat/completions")
                .restClientBuilder(RestClient.builder(restTemplate))
                .build();
    }

    @Bean("kimiApi")
    public AiCommonApi kimiApi(KimiConfig kiMiConfig,RestTemplate restTemplate){
        return AiCommonApi.builder()
                .apiKey(kiMiConfig.getApiKey())
                .baseUrl(kiMiConfig.getBaseUrl())
                .completionsPath("/v1/chat/completions")
                .restClientBuilder(RestClient.builder(restTemplate))
                .build();
    }

    @Bean("miniMaxApi")
    public AiCommonApi miniMaxApi(MiniMaxConfig miniMaxConfig,RestTemplate restTemplate){
        return AiCommonApi.builder()
                .apiKey(miniMaxConfig.getApiKey())
                .baseUrl(miniMaxConfig.getBaseUrl())
                .completionsPath("/v1/text/chatcompletion_v2")
                .restClientBuilder(RestClient.builder(restTemplate))
                .build();
    }

    @Primary
    @Bean("qwenEmbeddingModel")
    public QwenEmbeddingModel qwenEmbeddingModel(AiCommonApi qwenApi){
        return QwenEmbeddingModel.builder()
                .defaultOptions(
                        QwenEmbeddingOptions.builder()
                                .model(ChatModelEnum.QWEN_TEXT_EMBEDDING_4)
                                .build()
                )
                .aiCommonApi(qwenApi)
                .build();
    }

    @Bean("qwen30bModel")
    public QwenChatModel qwen30bModel(AiCommonApi qwenApi){
        return QwenChatModel.builder()
                .aiCommonApi(qwenApi)
                .options(
                        QwenChatOptions.builder()
                                .model(ChatModelEnum.QWEN_3_30B)
                                .temperature(0.5)
                                .maxTokens(4000)
                                .build()
                )
                .build();
    }

    /** 关键词提取转换器 */
    @Bean("keywordTransformer")
    public KeywordTransformer keywordTransformer(QwenChatModel qwen30bModel){
        return new KeywordTransformer(qwen30bModel);
    }


    /** 嵌入批处理策略 */
    @Bean("batchingStrategy")
    public BatchingStrategy batchingStrategy(){
        return new TokenCountBatchingStrategy(EncodingType.CL100K_BASE,8192,0.15);
    }
}

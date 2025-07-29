package com.agent.brick.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/7/18
 */
@Configuration
@ConfigurationProperties("dify")
@Data
public class DifyConfig {
    private String apiKey;

    private String baseUrl;

    /**
     * 知识库id
     */
    private String datasetId;
}

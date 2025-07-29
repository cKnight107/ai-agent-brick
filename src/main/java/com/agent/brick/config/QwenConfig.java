package com.agent.brick.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @since 2025/6/2
 * @author cKnight
 */
@Configuration
@ConfigurationProperties("spring.ai.qwen")
@Data
public class QwenConfig {
    private String apiKey;
    private String baseUrl;
    private String model;
}

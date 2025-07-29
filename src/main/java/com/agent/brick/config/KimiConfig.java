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
 * @since 2025/7/16
 */
@Configuration
@ConfigurationProperties("spring.ai.kimi")
@Data
public class KimiConfig {
    private String apiKey;
    private String baseUrl;
}

package com.agent.brick.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 智普AI配置
 * </p>
 *
 * @author cKnight
 * @since 2025/7/16
 */
@Configuration
@ConfigurationProperties("spring.ai.zhipu")
@Data
public class ZhiPuConfig {
    private String apiKey;
    private String baseUrl;
}

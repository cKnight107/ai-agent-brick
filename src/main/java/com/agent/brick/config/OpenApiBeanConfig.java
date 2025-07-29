package com.agent.brick.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

/**
 * <p>
 * open api bean
 * </p>
 *
 * @author cKnight
 * @since 2025/7/18
 */
@Configuration
@Slf4j
public class OpenApiBeanConfig {

    @Bean("difyHeaders")
    public HttpHeaders difyHeaders(DifyConfig difyConfig){
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, STR."Bearer \{difyConfig.getApiKey()}");
        return headers;
    }
}

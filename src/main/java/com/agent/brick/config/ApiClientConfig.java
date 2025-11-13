package com.agent.brick.config;

import com.agent.brick.api.DifyHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * <p>
 * 接口客户端配置
 * </p>
 *
 * @author cKnight
 * @since 2025/11/13
 */
@Configuration
@Slf4j
public class ApiClientConfig {

    @Bean("difyHttpClient")
    public DifyHttpClient difyHttpClient(DifyConfig difyConfig, JdkClientHttpRequestFactory defaultClientHttpRequestFactory) {
        RestClient restClient = RestClient.builder()
                .baseUrl(difyConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, STR."Bearer \{difyConfig.getApiKey()}")
                .requestFactory(defaultClientHttpRequestFactory)
                .requestInterceptor((r,b,e)->{
                    log.info("dify请求url:{}", r.getURI());
                    log.info("dify请求参数:{}",r.getAttributes());
                    return e.execute(r,b);
                })
                .build();
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        return proxyFactory.createClient(DifyHttpClient.class);
    }
}

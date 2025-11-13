package com.agent.brick.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * <p>
 * restClient 配置
 * </p>
 *
 * @author cKnight
 * @since 2025/11/13
 */
@Configuration
public class RestClientConfig {

    @Bean
    @Primary
    public JdkClientHttpRequestFactory defaultClientHttpRequestFactory(Executor virtualThreadExecutor) {
        HttpClient httpClient = HttpClient.newBuilder()
                //连接超时
                .connectTimeout(Duration.ofSeconds(10))
                //重定向策略
                .followRedirects(HttpClient.Redirect.NORMAL)
                //使用全局虚拟线程
                .executor(virtualThreadExecutor)
                //不强制使用 2 会优先使用2 若不支持则回退到1
//                .version(HttpClient.Version.HTTP_2)
                .build();
        // 使用 JdkClientHttpRequestFactory
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        //响应 超时时间
        requestFactory.setReadTimeout(Duration.ofSeconds(40));
        return requestFactory;
    }

    /**
     * 默认的RestClient
     * @return RestClient
     */
    @Bean
    @Primary
    public RestClient defaultRestClient(JdkClientHttpRequestFactory defaultClientHttpRequestFactory) {
        return RestClient.builder().requestFactory(defaultClientHttpRequestFactory).build();
    }
}

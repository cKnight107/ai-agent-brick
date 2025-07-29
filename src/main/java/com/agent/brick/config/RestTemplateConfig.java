package com.agent.brick.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * spring rest http 配置
 * @author  cKnight
 * @since 2024/6/11
 */
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory){
        return new RestTemplate(factory);
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(){
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    /**
     * 连接池配置
     * @return
     */
    @Bean
    public HttpClient httpClient(){
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();

        //创建连接管理器
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);

        //设置连接池最大连接数
        connectionManager.setMaxTotal(500);
        //此设置是对MaxTotal的细分，每个主机最大并发为300(DefaultMaxPerRoute) 但是多个主机相加并发不可超出500(MaxTotal)
        connectionManager.setDefaultMaxPerRoute(300);

        //创建请求
        RequestConfig requestConfig = RequestConfig.custom()
                //从连接池获取连接的超时时间。
                .setConnectionRequestTimeout(Timeout.ofMinutes(1))
                //等待服务器响应的超时时间。
                .setResponseTimeout(Timeout.ofMinutes(3))
                .build();

        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}

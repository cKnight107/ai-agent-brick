package com.agent.brick.config;

import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/11/14
 */
@Configuration
public class TomcatConfiguration {
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            // 使用虚拟线程来处理每一个请求
            ThreadFactory factory = Thread.ofVirtual()
                    .name("http-vt-", 0)
                    .factory();
            protocolHandler.setExecutor(Executors.newThreadPerTaskExecutor(factory));
        };
    }
}

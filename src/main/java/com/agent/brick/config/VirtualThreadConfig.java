package com.agent.brick.config;

import com.agent.brick.constants.GlobalConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * <p>
 * 虚拟线程配置
 * </p>
 *
 * @author cKnight
 * @since 2025/11/13
 */
@Configuration
public class VirtualThreadConfig {

    /**
     * 全局统一线程池
     * @return virtualThreadExecutor
     */
    @Bean(GlobalConstants.VIRTUAL_THREAD)
    @Primary
    public Executor virtualThreadExecutor() {
        ThreadFactory  threadFactory = Thread.ofVirtual()
                .name("app-vt-",0)
                .factory();
        return Executors.newThreadPerTaskExecutor(threadFactory);
    }


    /**
     * 全局 @Async 注解的虚拟线程
     * @return asyncVirtualThreadExecutor
     */
    @Bean(GlobalConstants.ASYNC_VIRTUAL_THREAD)
    public Executor asyncVirtualThreadExecutor() {
        ThreadFactory  threadFactory = Thread.ofVirtual()
                .name("async-vt-",0)
                .factory();
        return Executors.newThreadPerTaskExecutor(threadFactory);
    }
}

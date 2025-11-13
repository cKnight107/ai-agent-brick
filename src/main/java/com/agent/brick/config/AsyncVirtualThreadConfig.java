package com.agent.brick.config;

import com.agent.brick.constants.GlobalConstants;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/11/13
 */
@Configuration
@EnableAsync
public class AsyncVirtualThreadConfig implements AsyncConfigurer {

    @Resource
    @Qualifier(GlobalConstants.ASYNC_VIRTUAL_THREAD)
    private Executor executor;

    @Override
    public Executor getAsyncExecutor() {
        return executor;
    }
}

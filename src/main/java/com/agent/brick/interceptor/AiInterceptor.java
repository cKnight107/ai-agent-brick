package com.agent.brick.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @since 2025/6/17
 *
 * @author cKnight
 */
@Configuration
@Slf4j
public class AiInterceptor implements WebMvcConfigurer {

    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                //拦截哪些路径
                .addPathPatterns("/api/*/**")
                //排除哪些路径
                .excludePathPatterns("/api/*/user/login","/api/*/ai/completions");
    }

}

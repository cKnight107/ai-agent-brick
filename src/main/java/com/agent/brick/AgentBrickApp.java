package com.agent.brick;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/7/29
 */
@SpringBootApplication(
        exclude = FreeMarkerAutoConfiguration.class
)
@MapperScan("com.agent.brick.mapper")
public class AgentBrickApp {
    public static void main(String[] args) {
        SpringApplication.run(AgentBrickApp.class, args);
    }
}

package com.agent.brick.ai.prompt.annotation;

import java.lang.annotation.*;

/**
 * <p>
 *  prompt 工具注解
 * </p>
 * @author cKnight
 * @since 2025/8/7
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PromptTool {
    String[] rules() default {};
}

package com.agent.brick.ai;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.observation.DefaultChatModelObservationConvention;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;

/**
 * @since 2025/6/5
 *
 * @author cKnight
 */
public interface AiConstants {

   ChatModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultChatModelObservationConvention();

   ToolCallingManager DEFAULT_TOOL_CALLING_MANAGER = ToolCallingManager.builder().build();

    RetryTemplate RETRY_TEMPLATE = RetryUtils.DEFAULT_RETRY_TEMPLATE;

    ObservationRegistry OBSERVATION_REGISTRY = ObservationRegistry.NOOP;

    ToolExecutionEligibilityPredicate TOOL_EXECUTION_ELIGIBILITY_PREDICATE = new DefaultToolExecutionEligibilityPredicate();
}

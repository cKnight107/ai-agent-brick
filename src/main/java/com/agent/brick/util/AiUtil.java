package com.agent.brick.util;

import com.agent.brick.ai.AiCommonApi;
import com.agent.brick.ai.model.QwenChatModel;
import com.agent.brick.ai.model.optins.QwenChatOptions;
import com.agent.brick.ai.prompt.AgentPromptConstants;
import com.agent.brick.ai.prompt.MyPromptTemplate;
import com.agent.brick.ai.prompt.PromptConstants;
import com.agent.brick.ai.prompt.annotation.PromptTool;
import com.agent.brick.ai.prompt.record.Parameters;
import com.agent.brick.ai.prompt.record.PromptToolDefinition;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.controller.request.AiMessageReq;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.ai.model.enums.ChatModelEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeType;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ai 工具类
 * @since 2025/6/7
 *
 * @author cKnight
 */
@Slf4j
public class AiUtil {

    /**
     * 获取qwen model 默认 qwen3
     *
     * @param aiReq 入参
     * @return QwenChatModel
     */
    public static QwenChatModel genQwenModel(AiReq aiReq) {
        AiCommonApi qwenApi = SpringContextUtils.getBean("qwenApi", AiCommonApi.class);
        return QwenChatModel
                .builder()
                .aiCommonApi(qwenApi)
                .options(
                        QwenChatOptions.
                                builder()
                                .model(ChatModelEnum.QWEN_3)
                                .temperature(0.85)
                                .maxTokens(8048)
//                                .parallelToolCalls(true)
                                .build()
                                .enableThinking(aiReq.isThinkFlag())
                )
                .build();
    }

    /**
     * 动态生成提示词
     *
     * @param req 入参
     * @return 提示词
     */
    public static Prompt genUserPrompt(AiMessageReq req) {
        return CollectionUtils.isNotEmpty(req.getMedias()) ? MyPromptTemplate
                .start()
                .messageType(MessageType.USER)
                .template(PromptConstants.DEFAULT_USER_PROMPT_STR)
                .media(req.getMedias()
                        .stream()
                        .map(obj -> {
                            try {
                                return new Media(new MimeType(obj.getType()), new URI(obj.getUrl()));
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList())
                )
                .end()
                .create(Map.of(GlobalConstants.QUERY, req.getContent()))
                :
                PromptConstants.COT_TEMPLATE_SYSTEM.create(Map.of(GlobalConstants.QUERY, req.getContent()));
    }


    public static Message genAgentUserMessage(String query,String context) {
        if (StringUtils.isEmpty(context)){
            context = "";
        }
        return MyPromptTemplate.start()
                .messageType(MessageType.USER)
                .template(AgentPromptConstants.AGENT_USER_PROMPT_STR)
                .end()
                .createMessage(Map.of(GlobalConstants.QUERY, query,GlobalConstants.CONTEXT,context));
    }


    public static String strFormat(Map<String, Object> model, String template) {
        return strFormat(StTemplateRenderer.builder().build(), model, template);
    }

    /**
     * 字符串格式化
     * 默认格式
     *
     * @param stTemplateRenderer 字符串模版
     * @param model              变量
     * @param template           字符串模版
     * @return 格式化后的
     */
    public static String strFormat(StTemplateRenderer stTemplateRenderer, Map<String, Object> model, String template) {
        return stTemplateRenderer.apply(template, model);
    }

    /**
     * 获取对象的工具定义
     * @param objectList 对象列表
     * @return List<PromptToolDefinition>
     */
    public  static <T> List<PromptToolDefinition> getToolDefinition(List<T> objectList){
        return objectList.stream().map(toolObject -> Stream
                        .of(ReflectionUtils.getDeclaredMethods(
                                AopUtils.isAopProxy(toolObject) ? AopUtils.getTargetClass(toolObject) : toolObject.getClass()))
                        .filter(toolMethod -> toolMethod.isAnnotationPresent(Tool.class))
                        .filter(toolMethod -> !isFunctionalType(toolMethod))
                        .map(toolMethod -> {
                            ToolDefinition toolDefinition = ToolDefinitions.from(toolMethod);
                            PromptTool promptTool = toolMethod.getAnnotation(PromptTool.class);
                            //组装参数
                            List<Parameters> parametersList = new ArrayList<>();
                            for (int i = 0; i < toolMethod.getParameterCount(); i++) {
                                Parameter parameter = toolMethod.getParameters()[i];
                                if (!parameter.isAnnotationPresent(ToolParam.class)) {
                                    //只收集被ToolParam装饰的参数
                                    continue;
                                }
                                ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
                                String parameterName = parameter.getName();
                                Type parameterType = toolMethod.getGenericParameterTypes()[i];
                                if (parameterType instanceof Class<?> parameterClass
                                        && ClassUtils.isAssignable(parameterClass, ToolContext.class)) {
                                    //去除 ToolContext
                                    continue;
                                }
                                parametersList.add(new Parameters(parameterName,toolParam.description(),toolParam.required()));
                            }
                            if (Objects.nonNull(promptTool) && Objects.nonNull(promptTool.rules())) {
                                return new PromptToolDefinition(toolDefinition.name(),toolDefinition.description(),toolDefinition.inputSchema(),parametersList,List.of(promptTool.rules()));
                            }else {
                                return new PromptToolDefinition(toolDefinition.name(),toolDefinition.description(),toolDefinition.inputSchema(),parametersList,List.of());
                            }
                        })
                        .toArray(PromptToolDefinition[]::new))
                .flatMap(Stream::of)
                .toList();
    }

    public static boolean isFunctionalType(Method toolMethod) {
        var isFunction = ClassUtils.isAssignable(toolMethod.getReturnType(), Function.class)
                || ClassUtils.isAssignable(toolMethod.getReturnType(), Supplier.class)
                || ClassUtils.isAssignable(toolMethod.getReturnType(), Consumer.class);

        if (isFunction) {
            log.warn("Method {} is annotated with @Tool but returns a functional type. "
                    + "This is not supported and the method will be ignored.", toolMethod.getName());
        }
        return isFunction;
    }
}

package com.agent.brick.util;

import com.agent.brick.ai.AiCommonApi;
import com.agent.brick.ai.model.QwenChatModel;
import com.agent.brick.ai.model.optins.QwenChatOptions;
import com.agent.brick.ai.prompt.AgentPromptConstants;
import com.agent.brick.ai.prompt.MyPromptTemplate;
import com.agent.brick.ai.prompt.PromptConstants;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.controller.request.AiMessageReq;
import com.agent.brick.controller.request.AiReq;
import com.agent.brick.enums.ChatModelEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.util.MimeType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

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
}

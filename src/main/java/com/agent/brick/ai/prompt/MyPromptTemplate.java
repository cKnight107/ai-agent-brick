package com.agent.brick.ai.prompt;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.template.TemplateRenderer;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 自定义的PromptTemplate支持 message选择
 * @since 2025/6/3
 *
 * @author cKnight
 */
public class MyPromptTemplate extends PromptTemplate {
    private static final TemplateRenderer DEFAULT_TEMPLATE_RENDERER = StTemplateRenderer.builder().build();
    private final String template;
    private final MessageType messageType;
    private final TemplateRenderer renderer;
    private List<Media> media;

    public MyPromptTemplate(Resource resource, MessageType messageType, TemplateRenderer renderer, String template) {
        super(resource);
        this.template = template;
        this.messageType = messageType;
        this.renderer = renderer;
    }

    public MyPromptTemplate(String template, MessageType messageType, TemplateRenderer renderer, List<Media> media) {
        super(template);
        this.template = template;
        this.messageType = messageType;
        this.renderer = renderer;
        this.media = media;
    }

    public static Builder start() {
        return new Builder();
    }

    private Message currentMessage() {
        return currentMessage(new HashMap<>());
    }

    /**
     * 获取当前message
     *
     * @param model 变量
     * @return message
     */
    private Message currentMessage(Map<String, Object> model) {
        return switch (messageType) {
            case SYSTEM -> new SystemMessage(this.render(model));
            case ASSISTANT -> new AssistantMessage(this.render(model));
            default -> CollectionUtils.isNotEmpty(media) ?
                    UserMessage.builder().text(this.render(model)).media(media).build() : new UserMessage(this.render(model));
        };
    }

    @Override
    public String render() {
        return this.renderer.apply(this.template, new HashMap<>());
    }

    @Override
    public String render(Map<String, Object> additionalVariables) {
        Map<String, Object> combinedVariables = new HashMap<>();
        Iterator var3 = additionalVariables.entrySet().iterator();

        while (var3.hasNext()) {
            Map.Entry<String, Object> entry = (Map.Entry) var3.next();
            combinedVariables.put(entry.getKey(), entry.getValue());
        }
        return this.renderer.apply(this.template, combinedVariables);
    }

    @Override
    public Prompt create() {
        return new Prompt(currentMessage());
    }

    @Override
    public Prompt create(Map<String, Object> model) {
        return new Prompt(currentMessage(model));
    }

    public static final class Builder {
        private String template;
        private TemplateRenderer renderer;
        private MessageType messageType;
        private List<Media> media;

        private Builder() {
            this.renderer = DEFAULT_TEMPLATE_RENDERER;
        }

        public Builder template(String template) {
            Assert.hasText(template, "template 不可为空");
            this.template = template;
            return this;
        }

        public Builder renderer(TemplateRenderer renderer) {
            Assert.notNull(renderer, "renderer 不可为空");
            this.renderer = renderer;
            return this;
        }

        public Builder messageType(MessageType messageType) {
            Assert.notNull(renderer, "messageType 不可为空");
            this.messageType = messageType;
            return this;
        }

        public Builder media(List<Media> media) {
            this.media = media;
            return this;
        }

        public MyPromptTemplate end() {
            return new MyPromptTemplate(this.template, this.messageType, this.renderer, this.media);
        }
    }
}

package com.agent.brick.ai.agent;

import com.agent.brick.controller.request.AiReq;
import com.agent.brick.util.AiUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;

import java.util.*;

/**
 * agent父类
 * @since 2025/6/20
 *
 * @author cKnight
 */
@AllArgsConstructor
public abstract class AbstractAgent implements Agent {
    /**
     * 模型
     */
    protected ChatModel chatModel;
    /**
     * 系统提示词
     */
    protected Prompt prompt;
    /**
     * 代理名称
     */
    protected String agentName;
    /**
     * 代理介绍
     */
    protected String agentIntroduction;
    /**
     * 消息列表
     */
    protected List<Message> messages = new ArrayList<>();
    /**
     * 拦截器列表
     */
    protected List<Advisor> advisors = new ArrayList<>();
    /**
     * 工具列表
     */
    protected List<Object> tools = new ArrayList<>();

    public AbstractAgent() {
        this.chatModel = null;
        this.prompt = null;
        this.agentName = "";
        this.agentIntroduction = "";
    }


    @Override
    public ChatClient.ChatClientRequestSpec chatClient() {
        checkArgs();
        ChatClient.ChatClientRequestSpec chatClientRequestSpec = ChatClient.create(chatModel).prompt(Objects.isNull(prompt) ? getPrompt() : prompt);
        if (CollectionUtils.isNotEmpty(messages)) {
            chatClientRequestSpec.messages(messages);
        }
        if (CollectionUtils.isNotEmpty(advisors)) {
            chatClientRequestSpec.advisors(advisors);
        }
        if (CollectionUtils.isNotEmpty(tools)) {
            tools.forEach(chatClientRequestSpec::tools);
        }
        return chatClientRequestSpec;
    }

    @Override
    public ChatClient.CallResponseSpec call() {
        return this.chatClient().call();
    }

    @Override
    public ChatClient.StreamResponseSpec steam() {
        return this.chatClient().stream();
    }


    @Override
    public ChatClient.ChatClientRequestSpec chatClient(AiReq req) {
        Assert.notNull(req,"AiReq缺少");
        Assert.notNull(req.getMessage(),"AiReqMsg缺少");
        Assert.notNull(req.getMessage().getContent(),"AiReqMsgContent缺少");
        if (Objects.isNull(prompt)) {
            this.prompt = getPrompt(req);
        }
        return this.chatClient();
    }

    @Override
    public ChatClient.CallResponseSpec call(AiReq req) {
        return this.chatClient(req).call();
    }

    @Override
    public ChatClient.StreamResponseSpec steam(AiReq req) {
        return this.chatClient(req).stream();
    }

    @Override
    public Prompt getPrompt() {
        return null;
    }

    @Override
    public Prompt getPrompt(AiReq req) {
        return null;
    }

    /**
     * 输出自己
     */
    public String oneself() {
        Assert.notNull(agentName, "缺少名称");
        Assert.notNull(agentIntroduction, "缺少介绍");
        return AiUtil.strFormat(
                Map.of("agentName", this.agentName, "agentIntroduction", this.agentIntroduction),
                """
                        名称：{agentName}
                        角色：{agentIntroduction}
                        """
        );
    }

    /**
     * 核心参数校验
     */
    private void checkArgs() {
        Assert.notNull(chatModel, "模型不可为空");
//        Assert.notNull(prompt, "系统提示词不可为空");
    }

    public static class Builder<T extends AbstractAgent> {
        /**
         * 子类
         */
        T agent;

        public Builder(Class<T> clazz) {
            try {
                this.agent = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
            }
        }

        public Builder<T> chatModel(ChatModel chatModel) {
            this.agent.chatModel = chatModel;
            return this;
        }

        public Builder<T> prompt(Prompt prompt) {
            this.agent.prompt = prompt;
            return this;
        }

        public Builder<T> agentName(String agentName) {
            this.agent.agentName = agentName;
            return this;
        }

        public Builder<T> agentIntroduction(String agentIntroduction) {
            this.agent.agentIntroduction = agentIntroduction;
            return this;
        }

        public Builder<T> messages(List<Message> messages) {
            this.agent.messages.addAll(messages);
            return this;
        }

        public Builder<T> messages(Message... messages) {
            this.agent.messages.addAll(Arrays.asList(messages));
            return this;
        }

        public Builder<T> advisors(List<Advisor> advisors) {
            this.agent.advisors.addAll(advisors);
            return this;
        }

        public Builder<T> advisors(Advisor... advisors) {
            this.agent.advisors.addAll(Arrays.asList(advisors));
            return this;
        }

        public <E> Builder<T> toolList(List<E> tools) {
            this.agent.tools.addAll(tools);
            return this;
        }

        public Builder<T> tools(Object... tools) {
            this.agent.tools.addAll(Arrays.asList(tools));
            return this;
        }

        public T build() {
            return agent;
        }
    }

}

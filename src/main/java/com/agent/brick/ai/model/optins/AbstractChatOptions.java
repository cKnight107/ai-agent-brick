package com.agent.brick.ai.model.optins;

import com.agent.brick.ai.AiCommonApi;
import com.agent.brick.ai.model.enums.ChatModelEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;

/**
 * ai options 抽象父类
 * @see org.springframework.ai.openai.OpenAiChatOptions
 * @author OpenAiChatOptions
 */
@Data
public abstract class AbstractChatOptions implements ToolCallingChatOptions {
    protected @JsonProperty("model") String model;

    protected @JsonProperty("frequency_penalty") Double frequencyPenalty;

    protected @JsonProperty("logit_bias") Map<String, Integer> logitBias;

    protected @JsonProperty("logprobs") Boolean logprobs;

    protected @JsonProperty("top_logprobs") Integer topLogprobs;

    protected @JsonProperty("max_tokens") Integer maxTokens;

    protected @JsonProperty("max_completion_tokens") Integer maxCompletionTokens;

    protected @JsonProperty("n") Integer n;

    protected @JsonProperty("modalities") List<String> outputModalities;

    protected @JsonProperty("audio") AiCommonApi.ChatCompletionRequest.AudioParameters outputAudio;

    protected @JsonProperty("presence_penalty") Double presencePenalty;

    protected @JsonProperty("response_format") ResponseFormat responseFormat;

    protected @JsonProperty("stream_options") AiCommonApi.ChatCompletionRequest.StreamOptions streamOptions;

    protected @JsonProperty("seed") Integer seed;

    protected @JsonProperty("stop") List<String> stop;

    protected @JsonProperty("temperature") Double temperature;

    protected @JsonProperty("top_p") Double topP;

    protected @JsonProperty("tools") List<AiCommonApi.FunctionTool> tools;

    protected @JsonProperty("tool_choice") Object toolChoice;

    protected @JsonProperty("user") String user;

    protected @JsonProperty("parallel_tool_calls") Boolean parallelToolCalls;

    protected @JsonProperty("store") Boolean store;

    protected @JsonProperty("metadata") Map<String, String> metadata;

    protected @JsonProperty("reasoning_effort") String reasoningEffort;

    protected @JsonProperty("web_search_options") AiCommonApi.ChatCompletionRequest.WebSearchOptions webSearchOptions;

    @JsonIgnore
    protected List<ToolCallback> toolCallbacks = new ArrayList<>();

    @JsonIgnore
    protected Set<String> toolNames = new HashSet<>();

    @JsonIgnore
    protected Boolean internalToolExecutionEnabled;

    @JsonIgnore
    protected Map<String, String> httpHeaders = new HashMap<>();

    @JsonIgnore
    protected Map<String, Object> toolContext = new HashMap<>();


    @Override
    public List<String> getStopSequences() {
        return this.stop;
    }

    @Override
    public Integer getTopK() {
        return null;
    }

    /**
     * 复制方法 子类实现
     * @return T
     */
    @Override
    public  abstract AbstractChatOptions copy();


    public static class Builder<T extends AbstractChatOptions> {
        T options;

        public Builder(Class<T> clazz){
            try {
                this.options = clazz.getDeclaredConstructor().newInstance();
            }catch (Exception e){}
        }

        public Builder<T> model(String model) {
            this.options.model = model;
            return this;
        }

        public Builder<T> model(ChatModelEnum model) {
            this.options.model = model.value;
            return this;
        }

        public Builder<T> model(OpenAiApi.ChatModel openAiChatModel) {
            this.options.model = openAiChatModel.getName();
            return this;
        }

        public Builder<T> frequencyPenalty(Double frequencyPenalty) {
            this.options.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder<T> logitBias(Map<String, Integer> logitBias) {
            this.options.logitBias = logitBias;
            return this;
        }

        public Builder<T> logprobs(Boolean logprobs) {
            this.options.logprobs = logprobs;
            return this;
        }

        public Builder<T> topLogprobs(Integer topLogprobs) {
            this.options.topLogprobs = topLogprobs;
            return this;
        }

        public Builder<T> maxTokens(Integer maxTokens) {
            this.options.maxTokens = maxTokens;
            return this;
        }

        public Builder<T> maxCompletionTokens(Integer maxCompletionTokens) {
            this.options.maxCompletionTokens = maxCompletionTokens;
            return this;
        }

        public Builder<T> N(Integer n) {
            this.options.n = n;
            return this;
        }

        public Builder<T> outputModalities(List<String> modalities) {
            this.options.outputModalities = modalities;
            return this;
        }

        public Builder<T> outputAudio(AiCommonApi.ChatCompletionRequest.AudioParameters audio) {
            this.options.outputAudio = audio;
            return this;
        }

        public Builder<T> presencePenalty(Double presencePenalty) {
            this.options.presencePenalty = presencePenalty;
            return this;
        }

        public Builder<T> responseFormat(ResponseFormat responseFormat) {
            this.options.responseFormat = responseFormat;
            return this;
        }

        public Builder<T> streamUsage(boolean enableStreamUsage) {
            this.options.streamOptions = (enableStreamUsage) ? AiCommonApi.ChatCompletionRequest.StreamOptions.INCLUDE_USAGE : null;
            return this;
        }

        public Builder<T> seed(Integer seed) {
            this.options.seed = seed;
            return this;
        }

        public Builder<T> stop(List<String> stop) {
            this.options.stop = stop;
            return this;
        }

        public Builder<T> temperature(Double temperature) {
            this.options.temperature = temperature;
            return this;
        }

        public Builder<T> topP(Double topP) {
            this.options.topP = topP;
            return this;
        }

        public Builder<T> tools(List<AiCommonApi.FunctionTool> tools) {
            this.options.tools = tools;
            return this;
        }

        public Builder<T> toolChoice(Object toolChoice) {
            this.options.toolChoice = toolChoice;
            return this;
        }

        public Builder<T> user(String user) {
            this.options.user = user;
            return this;
        }

        public Builder<T> parallelToolCalls(Boolean parallelToolCalls) {
            this.options.parallelToolCalls = parallelToolCalls;
            return this;
        }

        public Builder<T> toolCallbacks(List<ToolCallback> toolCallbacks) {
            this.options.setToolCallbacks(toolCallbacks);
            return this;
        }

        public Builder<T> toolCallbacks(ToolCallback... toolCallbacks) {
            Assert.notNull(toolCallbacks, "toolCallbacks cannot be null");
            this.options.toolCallbacks.addAll(Arrays.asList(toolCallbacks));
            return this;
        }

        public Builder<T> toolNames(Set<String> toolNames) {
            Assert.notNull(toolNames, "toolNames cannot be null");
            this.options.setToolNames(toolNames);
            return this;
        }

        public Builder<T> toolNames(String... toolNames) {
            Assert.notNull(toolNames, "toolNames cannot be null");
            this.options.toolNames.addAll(Set.of(toolNames));
            return this;
        }

        public Builder<T> internalToolExecutionEnabled(@Nullable Boolean internalToolExecutionEnabled) {
            this.options.setInternalToolExecutionEnabled(internalToolExecutionEnabled);
            return this;
        }

        public Builder<T> httpHeaders(Map<String, String> httpHeaders) {
            this.options.httpHeaders = httpHeaders;
            return this;
        }

        public Builder<T> toolContext(Map<String, Object> toolContext) {
            if (this.options.toolContext == null) {
                this.options.toolContext = toolContext;
            }
            else {
                this.options.toolContext.putAll(toolContext);
            }
            return this;
        }

        public Builder<T> store(Boolean store) {
            this.options.store = store;
            return this;
        }

        public Builder<T> metadata(Map<String, String> metadata) {
            this.options.metadata = metadata;
            return this;
        }

        public Builder<T> reasoningEffort(String reasoningEffort) {
            this.options.reasoningEffort = reasoningEffort;
            return this;
        }

        public Builder<T> webSearchOptions(AiCommonApi.ChatCompletionRequest.WebSearchOptions webSearchOptions) {
            this.options.webSearchOptions = webSearchOptions;
            return this;
        }
        public T build(){
            return options;
        }
    }

}

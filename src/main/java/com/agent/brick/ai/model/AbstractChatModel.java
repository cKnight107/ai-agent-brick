package com.agent.brick.ai.model;

import com.agent.brick.ai.AiCommonApi;
import com.agent.brick.ai.AiConstants;
import com.agent.brick.ai.model.optins.AbstractChatOptions;
import com.agent.brick.ai.model.optins.QwenChatOptions;
import com.agent.brick.enums.BizCodeEnum;
import com.agent.brick.exception.BizException;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.openai.metadata.support.OpenAiResponseHeaderExtractor;
import org.springframework.ai.support.UsageCalculator;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * chat model 抽象父类
 * @see org.springframework.ai.openai.OpenAiChatModel
 * @author OpenAiChatModel
 */
@Data
@Slf4j
public abstract class AbstractChatModel implements ChatModel {

    protected RetryTemplate retryTemplate = AiConstants.RETRY_TEMPLATE;

    protected ObservationRegistry observationRegistry = AiConstants.OBSERVATION_REGISTRY;

    protected ToolCallingManager toolCallingManager  = AiConstants.DEFAULT_TOOL_CALLING_MANAGER;

    protected ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate = AiConstants.TOOL_EXECUTION_ELIGIBILITY_PREDICATE;

    protected ChatModelObservationConvention observationConvention = AiConstants.DEFAULT_OBSERVATION_CONVENTION;

    protected AiCommonApi aiCommonApi;

    protected AbstractChatOptions defaultOptions;

    private String fromAudioData(Object audioData) {
        if (audioData instanceof byte[] bytes) {
            return Base64.getEncoder().encodeToString(bytes);
        }
        throw new IllegalArgumentException("Unsupported audio data type: " + audioData.getClass().getSimpleName());
    }

    private AiCommonApi.ChatCompletionMessage.MediaContent mapToMediaContent(Media media) {
        var mimeType = media.getMimeType();
        if (MimeTypeUtils.parseMimeType("audio/mp3").equals(mimeType)) {
            return new AiCommonApi.ChatCompletionMessage.MediaContent(
                    new AiCommonApi.ChatCompletionMessage.MediaContent.InputAudio(fromAudioData(media.getData()), AiCommonApi.ChatCompletionMessage.MediaContent.InputAudio.Format.MP3));
        }
        if (MimeTypeUtils.parseMimeType("audio/wav").equals(mimeType)) {
            return new AiCommonApi.ChatCompletionMessage.MediaContent(
                    new AiCommonApi.ChatCompletionMessage.MediaContent.InputAudio(fromAudioData(media.getData()), AiCommonApi.ChatCompletionMessage.MediaContent.InputAudio.Format.WAV));
        }
        else {
            return new AiCommonApi.ChatCompletionMessage.MediaContent(
                    new AiCommonApi.ChatCompletionMessage.MediaContent.ImageUrl(this.fromMediaData(media.getMimeType(), media.getData())));
        }
    }

    private String fromMediaData(MimeType mimeType, Object mediaContentData) {
        if (mediaContentData instanceof byte[] bytes) {
            // Assume the bytes are an image. So, convert the bytes to a base64 encoded
            // following the prefix pattern.
            return String.format("data:%s;base64,%s", mimeType.toString(), Base64.getEncoder().encodeToString(bytes));
        }
        else if (mediaContentData instanceof String text) {
            // Assume the text is a URLs or a base64 encoded image prefixed by the user.
            return text;
        }
        else {
            throw new IllegalArgumentException(
                    "Unsupported media data type: " + mediaContentData.getClass().getSimpleName());
        }
    }

    AiCommonApi.ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {

        List<AiCommonApi.ChatCompletionMessage> chatCompletionMessages = prompt.getInstructions().stream().map(message -> {
            if (message.getMessageType() == MessageType.USER || message.getMessageType() == MessageType.SYSTEM) {
                Object content = message.getText();
                if (message instanceof UserMessage userMessage) {
                    if (!CollectionUtils.isEmpty(userMessage.getMedia())) {
                        List<AiCommonApi.ChatCompletionMessage.MediaContent> contentList = new ArrayList<>(List.of(new AiCommonApi.ChatCompletionMessage.MediaContent(message.getText())));

                        contentList.addAll(userMessage.getMedia().stream().map(this::mapToMediaContent).toList());

                        content = contentList;
                    }
                }

                return List.of(new AiCommonApi.ChatCompletionMessage(content,
                        AiCommonApi.ChatCompletionMessage.Role.valueOf(message.getMessageType().name())));
            }
            else if (message.getMessageType() == MessageType.ASSISTANT) {
                var assistantMessage = (AssistantMessage) message;
                List<AiCommonApi.ChatCompletionMessage.ToolCall> toolCalls = null;
                if (!CollectionUtils.isEmpty(assistantMessage.getToolCalls())) {
                    toolCalls = assistantMessage.getToolCalls().stream().map(toolCall -> {
                        var function = new AiCommonApi.ChatCompletionMessage.ChatCompletionFunction(toolCall.name(), toolCall.arguments());
                        return new AiCommonApi.ChatCompletionMessage.ToolCall(toolCall.id(), toolCall.type(), function);
                    }).toList();
                }
                AiCommonApi.ChatCompletionMessage.AudioOutput audioOutput = null;
                if (!CollectionUtils.isEmpty(assistantMessage.getMedia())) {
                    Assert.isTrue(assistantMessage.getMedia().size() == 1,
                            "Only one media content is supported for assistant messages");
                    audioOutput = new AiCommonApi.ChatCompletionMessage.AudioOutput(assistantMessage.getMedia().get(0).getId(), null, null, null);

                }
                return List.of(new AiCommonApi.ChatCompletionMessage(assistantMessage.getText(),
                        AiCommonApi.ChatCompletionMessage.Role.ASSISTANT, null, null, toolCalls, null, audioOutput, null,null));
            }
            else if (message.getMessageType() == MessageType.TOOL) {
                ToolResponseMessage toolMessage = (ToolResponseMessage) message;

                toolMessage.getResponses()
                        .forEach(response -> Assert.isTrue(response.id() != null, "ToolResponseMessage must have an id"));
                return toolMessage.getResponses()
                        .stream()
                        .map(tr -> new AiCommonApi.ChatCompletionMessage(tr.responseData(), AiCommonApi.ChatCompletionMessage.Role.TOOL, tr.name(),
                                tr.id(), null, null, null, null,null))
                        .toList();
            }
            else {
                throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
            }
        }).flatMap(List::stream).toList();

        AiCommonApi.ChatCompletionRequest request = new AiCommonApi.ChatCompletionRequest(chatCompletionMessages, stream);

        AbstractChatOptions requestOptions = (AbstractChatOptions) prompt.getOptions();
        request = ModelOptionsUtils.merge(requestOptions, request, AiCommonApi.ChatCompletionRequest.class);

        // Add the tool definitions to the request's tools parameter.
        List<ToolDefinition> toolDefinitions = this.toolCallingManager.resolveToolDefinitions(requestOptions);
        if (!CollectionUtils.isEmpty(toolDefinitions)) {
            request = ModelOptionsUtils.merge(
                    request,QwenChatOptions.builder().tools(this.getFunctionTools(toolDefinitions)).build(),
                    AiCommonApi.ChatCompletionRequest.class);
        }

        // Remove `streamOptions` from the request if it is not a streaming request
        if (request.streamOptions() != null && !stream) {
            log.warn("Removing streamOptions from the request as it is not a streaming request!");
            request = request.streamOptions(null);
        }

        return request;
    }

    private List<AiCommonApi.FunctionTool> getFunctionTools(List<ToolDefinition> toolDefinitions) {
        return toolDefinitions.stream().map(toolDefinition -> {
            var function = new AiCommonApi.FunctionTool.Function(toolDefinition.description(), toolDefinition.name(),
                    toolDefinition.inputSchema());
            return new AiCommonApi.FunctionTool(function);
        }).toList();
    }

    public ChatResponse internalCall(Prompt prompt, ChatResponse previousChatResponse) {

        AiCommonApi.ChatCompletionRequest request = createRequest(prompt, false);

        ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
                .prompt(prompt)
                .provider(OpenAiApiConstants.PROVIDER_NAME)
                .build();


        ChatResponse response = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION
                .observation(this.observationConvention, AiConstants.DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                        this.observationRegistry)
                .observe(() -> {

                    ResponseEntity<AiCommonApi.ChatCompletion> completionEntity = this.retryTemplate
                            .execute(ctx -> this.aiCommonApi.chatCompletionEntity(request, getAdditionalHttpHeaders(prompt)));

                    var chatCompletion = completionEntity.getBody();

                    if (chatCompletion == null) {
                        log.warn("No chat completion returned for prompt: {}", prompt);
                        return new ChatResponse(List.of());
                    }

                    List<AiCommonApi.ChatCompletion.Choice> choices = chatCompletion.choices();
                    if (choices == null) {
                        log.warn("No choices returned for prompt: {}", prompt);
                        return new ChatResponse(List.of());
                    }

                    // @formatter:off
                    List<Generation> generations = choices.stream().map(choice -> {
                        Map<String, Object> metadata = Map.of(
                                "id", chatCompletion.id() != null ? chatCompletion.id() : "",
                                "role", choice.message().role() != null ? choice.message().role().name() : "",
                                "index", choice.index(),
                                "finishReason", choice.finishReason() != null ? choice.finishReason().name() : "",
                                "refusal", StringUtils.hasText(choice.message().refusal()) ? choice.message().refusal() : "",
                                "annotations", choice.message().annotations() != null ? choice.message().annotations() : List.of());
                        return buildGeneration(choice, metadata, request);
                    }).toList();
                    // @formatter:on

                    RateLimit rateLimit = OpenAiResponseHeaderExtractor.extractAiResponseHeaders(completionEntity);

                    // Current usage
                    AiCommonApi.Usage usage = chatCompletion.usage();
                    Usage currentChatResponseUsage = usage != null ? getDefaultUsage(usage) : new EmptyUsage();
                    Usage accumulatedUsage = UsageCalculator.getCumulativeUsage(currentChatResponseUsage,
                            previousChatResponse);
                    ChatResponse chatResponse = new ChatResponse(generations,
                            from(chatCompletion, rateLimit, accumulatedUsage));

                    observationContext.setResponse(chatResponse);

                    return chatResponse;

                });

        if (this.toolExecutionEligibilityPredicate.isToolExecutionRequired(prompt.getOptions(), response)) {
            var toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, response);
            if (toolExecutionResult.returnDirect()) {
                // Return tool execution result directly to the client.
                return ChatResponse.builder()
                        .from(response)
                        .generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
                        .build();
            }
            else {
                // Send the tool execution result back to the model.
                return this.internalCall(new Prompt(toolExecutionResult.conversationHistory(), prompt.getOptions()),
                        response);
            }
        }

        return response;
    }

    private ChatResponseMetadata from(AiCommonApi.ChatCompletion result, RateLimit rateLimit, Usage usage) {
        Assert.notNull(result, "AbstractChaModel ChatCompletionResult must not be null");
        var builder = ChatResponseMetadata.builder()
                .id(result.id() != null ? result.id() : "")
                .usage(usage)
                .model(result.model() != null ? result.model() : "")
                .keyValue("created", result.created() != null ? result.created() : 0L)
                .keyValue("system-fingerprint", result.systemFingerprint() != null ? result.systemFingerprint() : "");
        if (rateLimit != null) {
            builder.rateLimit(rateLimit);
        }
        return builder.build();
    }

    private DefaultUsage getDefaultUsage(AiCommonApi.Usage usage) {
        return new DefaultUsage(usage.promptTokens(), usage.completionTokens(), usage.totalTokens(), usage);
    }

    private Generation buildGeneration(AiCommonApi.ChatCompletion.Choice choice, Map<String, Object> metadata, AiCommonApi.ChatCompletionRequest request) {
        List<AssistantMessage.ToolCall> toolCalls = choice.message().toolCalls() == null ? List.of()
                : choice.message()
                .toolCalls()
                .stream()
                .map(toolCall -> new AssistantMessage.ToolCall(toolCall.id(), "function",
                        toolCall.function().name(), toolCall.function().arguments()))
                .toList();

        String finishReason = (choice.finishReason() != null ? choice.finishReason().name() : "");
        var generationMetadataBuilder = ChatGenerationMetadata.builder().finishReason(finishReason);

        List<Media> media = new ArrayList<>();
        String textContent = choice.message().content();
        var audioOutput = choice.message().audioOutput();
        if (audioOutput != null) {
            String mimeType = String.format("audio/%s", request.audioParameters().format().name().toLowerCase());
            byte[] audioData = Base64.getDecoder().decode(audioOutput.data());
            Resource resource = new ByteArrayResource(audioData);
            Media.builder().mimeType(MimeTypeUtils.parseMimeType(mimeType)).data(resource).id(audioOutput.id()).build();
            media.add(Media.builder()
                    .mimeType(MimeTypeUtils.parseMimeType(mimeType))
                    .data(resource)
                    .id(audioOutput.id())
                    .build());
            if (!StringUtils.hasText(textContent)) {
                textContent = audioOutput.transcript();
            }
            generationMetadataBuilder.metadata("audioId", audioOutput.id());
            generationMetadataBuilder.metadata("audioExpiresAt", audioOutput.expiresAt());
        }

        if (Boolean.TRUE.equals(request.logprobs())) {
            generationMetadataBuilder.metadata("logprobs", choice.logprobs());
        }

        var assistantMessage = new AssistantMessage(textContent, metadata, toolCalls, media);
        return new Generation(assistantMessage, generationMetadataBuilder.build());
    }

    private MultiValueMap<String, String> getAdditionalHttpHeaders(Prompt prompt) {

        Map<String, String> headers = new HashMap<>(this.defaultOptions.getHttpHeaders());
        if (prompt.getOptions() != null && prompt.getOptions() instanceof AbstractChatOptions chatOptions) {
            headers.putAll(chatOptions.getHttpHeaders());
        }
        return CollectionUtils.toMultiValueMap(
                headers.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> List.of(e.getValue()))));
    }

    protected Map<String, String> mergeHttpHeaders(Map<String, String> runtimeHttpHeaders,
                                                 Map<String, String> defaultHttpHeaders) {
        var mergedHttpHeaders = new HashMap<>(defaultHttpHeaders);
        mergedHttpHeaders.putAll(runtimeHttpHeaders);
        return mergedHttpHeaders;
    }



    protected Prompt buildRequestPrompt(Prompt prompt){
        // Process runtime options
        AbstractChatOptions runtimeOptions = convertOption(prompt);

        // Define request options by merging runtime options and default options
        AbstractChatOptions requestOptions = mergeOptions(runtimeOptions);

        // Merge @JsonIgnore-annotated options explicitly since they are ignored by
        // Jackson, used by ModelOptionsUtils.
        if (runtimeOptions != null) {
            if (runtimeOptions.getTopK() != null) {
                log.warn("The topK option is not supported by AbstractChaModel chat models. Ignoring.");
            }

            requestOptions.setHttpHeaders(
                    mergeHttpHeaders(runtimeOptions.getHttpHeaders(), this.defaultOptions.getHttpHeaders()));
            requestOptions.setInternalToolExecutionEnabled(
                    ModelOptionsUtils.mergeOption(runtimeOptions.getInternalToolExecutionEnabled(),
                            this.defaultOptions.getInternalToolExecutionEnabled()));
            requestOptions.setToolNames(ToolCallingChatOptions.mergeToolNames(runtimeOptions.getToolNames(),
                    this.defaultOptions.getToolNames()));
            requestOptions.setToolCallbacks(ToolCallingChatOptions.mergeToolCallbacks(runtimeOptions.getToolCallbacks(),
                    this.defaultOptions.getToolCallbacks()));
            requestOptions.setToolContext(ToolCallingChatOptions.mergeToolContext(runtimeOptions.getToolContext(),
                    this.defaultOptions.getToolContext()));
        }
        else {
            requestOptions.setHttpHeaders(this.defaultOptions.getHttpHeaders());
            requestOptions.setInternalToolExecutionEnabled(this.defaultOptions.getInternalToolExecutionEnabled());
            requestOptions.setToolNames(this.defaultOptions.getToolNames());
            requestOptions.setToolCallbacks(this.defaultOptions.getToolCallbacks());
            requestOptions.setToolContext(this.defaultOptions.getToolContext());
        }

        ToolCallingChatOptions.validateToolCallbacks(requestOptions.getToolCallbacks());

        return new Prompt(prompt.getInstructions(), requestOptions);
    }


    /**
     * Convert the ChatCompletionChunk into a ChatCompletion. The Usage is set to null.
     * @param chunk the ChatCompletionChunk to convert
     * @return the ChatCompletion
     */
    private AiCommonApi.ChatCompletion chunkToChatCompletion(AiCommonApi.ChatCompletionChunk chunk) {
        List<AiCommonApi.ChatCompletion.Choice> choices = chunk.choices()
                .stream()
                .map(chunkChoice -> new AiCommonApi.ChatCompletion.Choice(chunkChoice.finishReason(), chunkChoice.index(), chunkChoice.delta(),
                        chunkChoice.logprobs()))
                .toList();

        return new AiCommonApi.ChatCompletion(chunk.id(), choices, chunk.created(), chunk.model(), chunk.serviceTier(),
                chunk.systemFingerprint(), "chat.completion", chunk.usage());
    }

    private ChatResponseMetadata from(ChatResponseMetadata chatResponseMetadata, Usage usage) {
        Assert.notNull(chatResponseMetadata, "AbstractChaModel ChatResponseMetadata must not be null");
        var builder = ChatResponseMetadata.builder()
                .id(chatResponseMetadata.getId() != null ? chatResponseMetadata.getId() : "")
                .usage(usage)
                .model(chatResponseMetadata.getModel() != null ? chatResponseMetadata.getModel() : "");
        if (chatResponseMetadata.getRateLimit() != null) {
            builder.rateLimit(chatResponseMetadata.getRateLimit());
        }
        return builder.build();
    }

    protected Flux<ChatResponse> internalStream(Prompt prompt, ChatResponse previousChatResponse) {
        return Flux.deferContextual(contextView -> {
            AiCommonApi.ChatCompletionRequest request = createRequest(prompt, true);

            if (request.outputModalities() != null) {
                if (request.outputModalities().stream().anyMatch(m -> m.equals("audio"))) {
                    log.warn("Audio output is not supported for streaming requests. Removing audio output.");
                    throw new IllegalArgumentException("Audio output is not supported for streaming requests.");
                }
            }
            if (request.audioParameters() != null) {
                log.warn("Audio parameters are not supported for streaming requests. Removing audio parameters.");
                throw new IllegalArgumentException("Audio parameters are not supported for streaming requests.");
            }

            Flux<AiCommonApi.ChatCompletionChunk> completionChunks = this.aiCommonApi.chatCompletionStream(request,
                    getAdditionalHttpHeaders(prompt));

            // For chunked responses, only the first chunk contains the choice role.
            // The rest of the chunks with same ID share the same role.
            ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();

            final ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
                    .prompt(prompt)
                    .provider(OpenAiApiConstants.PROVIDER_NAME)
                    .build();

            Observation observation = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION.observation(
                    this.observationConvention, AiConstants.DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                    this.observationRegistry);

            observation.parentObservation(contextView.getOrDefault(ObservationThreadLocalAccessor.KEY, null)).start();

            // Convert the ChatCompletionChunk into a ChatCompletion to be able to reuse
            // the function call handling logic.
            Flux<ChatResponse> chatResponse = completionChunks.map(this::chunkToChatCompletion)
                    .switchMap(chatCompletion -> Mono.just(chatCompletion).map(chatCompletion2 -> {
                        try {
                            // If an id is not provided, set to "NO_ID" (for compatible APIs).
                            String id = chatCompletion2.id() == null ? "NO_ID" : chatCompletion2.id();

                            List<Generation> generations = chatCompletion2.choices().stream().map(choice -> { // @formatter:off
                                if (choice.message().role() != null) {
                                    roleMap.putIfAbsent(id, choice.message().role().name());
                                }
                                Map<String, Object> metadata = Map.of(
                                        "id", id,
                                        "role", roleMap.getOrDefault(id, ""),
                                        "index", choice.index(),
                                        "finishReason", choice.finishReason() != null ? choice.finishReason().name() : "",
                                        "refusal", StringUtils.hasText(choice.message().refusal()) ? choice.message().refusal() : "",
                                        "reasoningContent", StringUtils.hasText(choice.message().reasoningContent()) ? choice.message().reasoningContent() : "",
                                        "annotations", choice.message().annotations() != null ? choice.message().annotations() : List.of());
                                return buildGeneration(choice, metadata, request);
                            }).toList();
                            // @formatter:on
                            AiCommonApi.Usage usage = chatCompletion2.usage();
                            Usage currentChatResponseUsage = usage != null ? getDefaultUsage(usage) : new EmptyUsage();
                            Usage accumulatedUsage = UsageCalculator.getCumulativeUsage(currentChatResponseUsage,
                                    previousChatResponse);
                            return new ChatResponse(generations, from(chatCompletion2, null, accumulatedUsage));
                        }
                        catch (Exception e) {
                            log.error("Error processing chat completion", e);
                            return new ChatResponse(List.of());
                        }
                        // When in stream mode and enabled to include the usage, the AbstractChaModel
                        // Chat completion response would have the usage set only in its
                        // final response. Hence, the following overlapping buffer is
                        // created to store both the current and the subsequent response
                        // to accumulate the usage from the subsequent response.
                    }))
                    .buffer(2, 1)
                    .map(bufferList -> {
                        ChatResponse firstResponse = bufferList.get(0);
                        if (request.streamOptions() != null && request.streamOptions().includeUsage()) {
                            if (bufferList.size() == 2) {
                                ChatResponse secondResponse = bufferList.get(1);
                                if (secondResponse != null && secondResponse.getMetadata() != null) {
                                    // This is the usage from the final Chat response for a
                                    // given Chat request.
                                    Usage usage = secondResponse.getMetadata().getUsage();
                                    if (!UsageCalculator.isEmpty(usage)) {
                                        // Store the usage from the final response to the
                                        // penultimate response for accumulation.
                                        return new ChatResponse(firstResponse.getResults(),
                                                from(firstResponse.getMetadata(), usage));
                                    }
                                }
                            }
                        }
                        return firstResponse;
                    });

            // @formatter:off
            Flux<ChatResponse> flux = chatResponse.flatMap(response -> {
                        if (this.toolExecutionEligibilityPredicate.isToolExecutionRequired(prompt.getOptions(), response)) {
                            return Flux.defer(() -> {
                                // FIXME: bounded elastic needs to be used since tool calling
                                //  is currently only synchronous
                                var toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, response);
                                if (toolExecutionResult.returnDirect()) {
                                    // Return tool execution result directly to the client.
                                    return Flux.just(ChatResponse.builder().from(response)
                                            .generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
                                            .build());
                                }
                                else {
                                    // Send the tool execution result back to the model.
                                    return this.internalStream(new Prompt(toolExecutionResult.conversationHistory(), prompt.getOptions()),
                                            response);
                                }
                            }).subscribeOn(Schedulers.boundedElastic());
                        }
                        else {
                            return Flux.just(response);
                        }
                    })
                    .doOnError(observation::error)
                    .doFinally(s -> observation.stop())
                    .contextWrite(ctx -> ctx.put(ObservationThreadLocalAccessor.KEY, observation));
            // @formatter:on

            return new MessageAggregator().aggregate(flux, observationContext::setResponse);

        });
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        check();
        Prompt requestPrompt = buildRequestPrompt(prompt);
        return this.internalCall(requestPrompt, null);
    }


    /**
     * 转换参数
     * @return option
     */
    protected abstract AbstractChatOptions convertOption(Prompt prompt);

    /**
     * 合并option
     * @param runtimeOptions 运行中的
     * @return merge 后的 option
     */
    protected abstract AbstractChatOptions mergeOptions(AbstractChatOptions runtimeOptions);

    
    @Override
    public abstract ChatOptions getDefaultOptions();

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        check();
        Prompt requestPrompt = buildRequestPrompt(prompt);
        return internalStream(requestPrompt, null);
    }

    private void check(){
        if (Objects.isNull(this.defaultOptions) ||Objects.isNull(this.aiCommonApi)){
            throw new BizException(BizCodeEnum.SYS_ARGS_ERROR);
        }
    }

    public static class Builder<T extends AbstractChatModel>{
        T chatModel;

        public Builder(Class<T> clazz){
            try {
                this.chatModel = clazz.getDeclaredConstructor().newInstance();
            }catch (Exception e){}
        }

        public Builder<T> aiCommonApi(AiCommonApi aiCommonApi){
            this.chatModel.aiCommonApi = aiCommonApi;
            return this;
        }

        public Builder<T> options(AbstractChatOptions chatOptions){
            this.chatModel.defaultOptions = chatOptions;
            return this;
        }

        public Builder<T> retryTemplate(RetryTemplate retryTemplate){
            this.chatModel.retryTemplate = retryTemplate;
            return this;
        }

        public Builder<T> observationRegistry(ObservationRegistry observationRegistry){
            this.chatModel.observationRegistry = observationRegistry;
            return this;
        }

        public Builder<T> toolCallingManager(ToolCallingManager toolCallingManager){
            this.chatModel.toolCallingManager = toolCallingManager;
            return this;
        }

        public Builder<T> toolExecutionEligibilityPredicate(ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate){
            this.chatModel.toolExecutionEligibilityPredicate = toolExecutionEligibilityPredicate;
            return this;
        }

        public Builder<T> observationConvention(ChatModelObservationConvention observationConvention){
            this.chatModel.observationConvention = observationConvention;
            return this;
        }

        public T build(){
            return chatModel;
        }
    }
}

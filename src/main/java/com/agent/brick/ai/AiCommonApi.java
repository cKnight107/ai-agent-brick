package com.agent.brick.ai;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @see OpenAiApi
 * @author OpenAiApi
 */
@Getter
public class AiCommonApi {

    /**
     * Returns a builder pre-populated with the current configuration for mutation.
     */
    public Builder mutate() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final String DEFAULT_EMBEDDING_MODEL = OpenAiApi.EmbeddingModel.TEXT_EMBEDDING_ADA_002.getValue();

    private static final Predicate<String> SSE_DONE_PREDICATE = "[DONE]"::equals;

    // Store config fields for mutate/copy
    private final String baseUrl;

    private final ApiKey apiKey;

    private final MultiValueMap<String, String> headers;

    private final String completionsPath;

    private final String embeddingsPath;

    private final ResponseErrorHandler responseErrorHandler;

    private final RestClient restClient;

    private final WebClient webClient;

    private CommonStreamFunctionCallingHelper chunkMerger = new CommonStreamFunctionCallingHelper();


    /**
     * Create a new chat completion api.
     *
     * @param baseUrl              api base URL.
     * @param apiKey               OpenAI apiKey.
     * @param headers              the http headers to use.
     * @param completionsPath      the path to the chat completions endpoint.
     * @param embeddingsPath       the path to the embeddings endpoint.
     * @param restClientBuilder    RestClient builder.
     * @param webClientBuilder     WebClient builder.
     * @param responseErrorHandler Response error handler.
     */
    public AiCommonApi(String baseUrl, ApiKey apiKey, MultiValueMap<String, String> headers, String completionsPath,
                       String embeddingsPath, RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder,
                       ResponseErrorHandler responseErrorHandler) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.headers = headers;
        this.completionsPath = completionsPath;
        this.embeddingsPath = embeddingsPath;
        this.responseErrorHandler = responseErrorHandler;

        Assert.hasText(completionsPath, "Completions Path must not be null");
        Assert.hasText(embeddingsPath, "Embeddings Path must not be null");
        Assert.notNull(headers, "Headers must not be null");

        // @formatter:off
        Consumer<HttpHeaders> finalHeaders = h -> {
            if (!(apiKey instanceof NoopApiKey)) {
                h.setBearerAuth(apiKey.getValue());
            }

            h.setContentType(MediaType.APPLICATION_JSON);
            h.addAll(headers);
        };
        this.restClient = restClientBuilder.clone()
                .baseUrl(baseUrl)
                .defaultHeaders(finalHeaders)
                .defaultStatusHandler(responseErrorHandler)

                .build();

        this.webClient = webClientBuilder.clone()
                .baseUrl(baseUrl)
                .defaultHeaders(finalHeaders)
                .build(); // @formatter:on
    }

    public static String getTextContent(List<ChatCompletionMessage.MediaContent> content) {
        return content.stream()
                .filter(c -> "text".equals(c.type()))
                .map(ChatCompletionMessage.MediaContent::text)
                .reduce("", (a, b) -> a + b);
    }

    /**
     * Creates a model response for the given chat conversation.
     * @param chatRequest The chat completion request.
     * @return Entity response with {@link ChatCompletion} as a body and HTTP status code
     * and headers.
     */
    public ResponseEntity<ChatCompletion> chatCompletionEntity(ChatCompletionRequest chatRequest) {
        return chatCompletionEntity(chatRequest, new LinkedMultiValueMap<>());
    }

    /**
     * Creates a model response for the given chat conversation.
     * @param chatRequest The chat completion request.
     * @param additionalHttpHeader Optional, additional HTTP headers to be added to the
     * request.
     * @return Entity response with {@link ChatCompletion} as a body and HTTP status code
     * and headers.
     */
    public ResponseEntity<ChatCompletion> chatCompletionEntity(ChatCompletionRequest chatRequest,
                                                               MultiValueMap<String, String> additionalHttpHeader) {

        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(!chatRequest.stream(), "Request must set the stream property to false.");
        Assert.notNull(additionalHttpHeader, "The additional HTTP headers can not be null.");

        return this.restClient.post()
                .uri(this.completionsPath)
                .headers(headers -> headers.addAll(additionalHttpHeader))
                .body(chatRequest)
                .retrieve()
                .toEntity(ChatCompletion.class);
    }

    /**
     * Creates a streaming chat response for the given chat conversation.
     * @param chatRequest The chat completion request. Must have the stream property set
     * to true.
     * @return Returns a {@link Flux} stream from chat completion chunks.
     */
    public Flux<ChatCompletionChunk> chatCompletionStream(ChatCompletionRequest chatRequest) {
        return chatCompletionStream(chatRequest, new LinkedMultiValueMap<>());
    }

    /**
     * Creates a streaming chat response for the given chat conversation.
     * @param chatRequest The chat completion request. Must have the stream property set
     * to true.
     * @param additionalHttpHeader Optional, additional HTTP headers to be added to the
     * request.
     * @return Returns a {@link Flux} stream from chat completion chunks.
     */
    public Flux<ChatCompletionChunk> chatCompletionStream(ChatCompletionRequest chatRequest,
                                                          MultiValueMap<String, String> additionalHttpHeader) {

        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream(), "Request must set the stream property to true.");

        AtomicBoolean isInsideTool = new AtomicBoolean(false);

        return this.webClient.post()
                .uri(this.completionsPath)
                .headers(headers -> headers.addAll(additionalHttpHeader))
                .body(Mono.just(chatRequest), ChatCompletionRequest.class)
                .retrieve()
                .bodyToFlux(String.class)
                // cancels the flux stream after the "[DONE]" is received.
                .takeUntil(SSE_DONE_PREDICATE)
                // filters out the "[DONE]" message.
                .filter(SSE_DONE_PREDICATE.negate())
                .map(content -> ModelOptionsUtils.jsonToObject(content, ChatCompletionChunk.class))
                // Detect is the chunk is part of a streaming function call.
                .map(chunk -> {
                    if (this.chunkMerger.isStreamingToolFunctionCall(chunk)) {
                        isInsideTool.set(true);
                    }
                    return chunk;
                })
                // Group all chunks belonging to the same function call.
                // Flux<ChatCompletionChunk> -> Flux<Flux<ChatCompletionChunk>>
                .windowUntil(chunk -> {
                    if (isInsideTool.get() && this.chunkMerger.isStreamingToolFunctionCallFinish(chunk)) {
                        isInsideTool.set(false);
                        return true;
                    }
                    return !isInsideTool.get();
                })
                // Merging the window chunks into a single chunk.
                // Reduce the inner Flux<ChatCompletionChunk> window into a single
                // Mono<ChatCompletionChunk>,
                // Flux<Flux<ChatCompletionChunk>> -> Flux<Mono<ChatCompletionChunk>>
                .concatMapIterable(window -> {
                    Mono<ChatCompletionChunk> monoChunk = window.reduce(
                            new ChatCompletionChunk(null, null, null, null, null, null, null, null),
                            (previous, current) -> this.chunkMerger.merge(previous, current));
                    return List.of(monoChunk);
                })
                // Flux<Mono<ChatCompletionChunk>> -> Flux<ChatCompletionChunk>
                .flatMap(mono -> mono);
    }

    /**
     * Creates an embedding vector representing the input text or token array.
     * @param embeddingRequest The embedding request.
     * @return Returns list of {@link Embedding} wrapped in {@link EmbeddingList}.
     * @param <T> Type of the entity in the data list. Can be a {@link String} or
     * {@link List} of tokens (e.g. Integers). For embedding multiple inputs in a single
     * request, You can pass a {@link List} of {@link String} or {@link List} of
     * {@link List} of tokens. For example:
     *
     * <pre>{@code List.of("text1", "text2", "text3") or List.of(List.of(1, 2, 3), List.of(3, 4, 5))} </pre>
     */
    public <T> ResponseEntity<EmbeddingList<Embedding>> embeddings(EmbeddingRequest<T> embeddingRequest) {

        Assert.notNull(embeddingRequest, "The request body can not be null.");

        // Input text to embed, encoded as a string or array of tokens. To embed multiple
        // inputs in a single
        // request, pass an array of strings or array of token arrays.
        Assert.notNull(embeddingRequest.input(), "The input can not be null.");
        Assert.isTrue(embeddingRequest.input() instanceof String || embeddingRequest.input() instanceof List,
                "The input must be either a String, or a List of Strings or List of List of integers.");

        // The input must not exceed the max input tokens for the model (8192 tokens for
        // text-embedding-ada-002), cannot
        // be an empty string, and any array must be 2048 dimensions or less.
        if (embeddingRequest.input() instanceof List list) {
            Assert.isTrue(!CollectionUtils.isEmpty(list), "The input list can not be empty.");
            Assert.isTrue(list.size() <= 2048, "The list must be 2048 dimensions or less");
            Assert.isTrue(
                    list.get(0) instanceof String || list.get(0) instanceof Integer || list.get(0) instanceof List,
                    "The input must be either a String, or a List of Strings or list of list of integers.");
        }

        return this.restClient.post()
                .uri(this.embeddingsPath)
                .body(embeddingRequest)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {

                });
    }


    /**
     * The type of modality for the model completion.
     */
    public enum OutputModality {

        // @formatter:off
        @JsonProperty("audio")
        AUDIO,
        @JsonProperty("text")
        TEXT
        // @formatter:on

    }


    /**
     * The reason the model stopped generating tokens.
     */
    public enum ChatCompletionFinishReason {

        /**
         * The model hit a natural stop point or a provided stop sequence.
         */
        @JsonProperty("stop")
        STOP,
        /**
         * The maximum number of tokens specified in the request was reached.
         */
        @JsonProperty("length")
        LENGTH,
        /**
         * The content was omitted due to a flag from our content filters.
         */
        @JsonProperty("content_filter")
        CONTENT_FILTER,
        /**
         * The model called a tool.
         */
        @JsonProperty("tool_calls")
        TOOL_CALLS,
        /**
         * Only for compatibility with Mistral AI API.
         */
        @JsonProperty("tool_call")
        TOOL_CALL
    }

    /**
     * Represents a tool the model may call. Currently, only functions are supported as a
     * tool.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public static class FunctionTool {

        /**
         * The type of the tool. Currently, only 'function' is supported.
         */
        @JsonProperty("type")
        private Type type = Type.FUNCTION;

        /**
         * The function definition.
         */
        @JsonProperty("function")
        private Function function;

        public FunctionTool() {

        }

        /**
         * Create a tool of type 'function' and the given function definition.
         *
         * @param type     the tool type
         * @param function function definition
         */
        public FunctionTool(Type type, Function function) {
            this.type = type;
            this.function = function;
        }

        /**
         * Create a tool of type 'function' and the given function definition.
         *
         * @param function function definition.
         */
        public FunctionTool(Function function) {
            this(Type.FUNCTION, function);
        }

        /**
         * Create a tool of type 'function' and the given function definition.
         */
        public enum Type {

            /**
             * Function tool type.
             */
            @JsonProperty("function")
            FUNCTION

        }

        /**
         * Function definition.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Function {

            @Setter
            @Getter
            @JsonProperty("strict")
            Boolean strict;
            @Setter
            @Getter
            @JsonProperty("description")
            private String description;
            @Setter
            @Getter
            @JsonProperty("name")
            private String name;
            @Setter
            @Getter
            @JsonProperty("parameters")
            private Map<String, Object> parameters;
            @JsonIgnore
            private String jsonSchema;

            /**
             * NOTE: Required by Jackson, JSON deserialization!
             */
            @SuppressWarnings("unused")
            private Function() {
            }

            /**
             * Create tool function definition.
             *
             * @param description A description of what the function does, used by the
             *                    model to choose when and how to call the function.
             * @param name        The name of the function to be called. Must be a-z, A-Z, 0-9,
             *                    or contain underscores and dashes, with a maximum length of 64.
             * @param parameters  The parameters the functions accepts, described as a JSON
             *                    Schema object. To describe a function that accepts no parameters, provide
             *                    the value {"type": "object", "properties": {}}.
             * @param strict      Whether to enable strict schema adherence when generating the
             *                    function call. If set to true, the model will follow the exact schema
             *                    defined in the parameters field. Only a subset of JSON Schema is supported
             *                    when strict is true.
             */
            public Function(String description, String name, Map<String, Object> parameters, Boolean strict) {
                this.description = description;
                this.name = name;
                this.parameters = parameters;
                this.strict = strict;
            }

            /**
             * Create tool function definition.
             *
             * @param description tool function description.
             * @param name        tool function name.
             * @param jsonSchema  tool function schema as json.
             */
            public Function(String description, String name, String jsonSchema) {
                this(description, name, ModelOptionsUtils.jsonToMap(jsonSchema), null);
            }

            public void setJsonSchema(String jsonSchema) {
                this.jsonSchema = jsonSchema;
                if (jsonSchema != null) {
                    this.parameters = ModelOptionsUtils.jsonToMap(jsonSchema);
                }
            }

        }

    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ChatCompletionRequest(
            @JsonProperty("messages") List<ChatCompletionMessage> messages,
            @JsonProperty("model") String model,
            @JsonProperty("store") Boolean store,
            @JsonProperty("metadata") Map<String, String> metadata,
            @JsonProperty("frequency_penalty") Double frequencyPenalty,
            @JsonProperty("logit_bias") Map<String, Integer> logitBias,
            @JsonProperty("logprobs") Boolean logprobs,
            @JsonProperty("top_logprobs") Integer topLogprobs,
            @JsonProperty("max_tokens") Integer maxTokens, // original field for specifying token usage.
            @JsonProperty("max_completion_tokens") Integer maxCompletionTokens,
            // new field for gpt-o1 and other reasoning models
            @JsonProperty("n") Integer n,
            @JsonProperty("modalities") List<OutputModality> outputModalities,
            @JsonProperty("audio") AudioParameters audioParameters,
            @JsonProperty("presence_penalty") Double presencePenalty,
            @JsonProperty("response_format") ResponseFormat responseFormat,
            @JsonProperty("seed") Integer seed,
            @JsonProperty("service_tier") String serviceTier,
            @JsonProperty("stop") List<String> stop,
            @JsonProperty("stream") Boolean stream,
            @JsonProperty("stream_options") StreamOptions streamOptions,
            @JsonProperty("temperature") Double temperature,
            @JsonProperty("top_p") Double topP,
            @JsonProperty("tools") List<FunctionTool> tools,
            @JsonProperty("tool_choice") Object toolChoice,
            @JsonProperty("parallel_tool_calls") Boolean parallelToolCalls,
            @JsonProperty("user") String user,
            @JsonProperty("reasoning_effort") String reasoningEffort,
            @JsonProperty("web_search_options") WebSearchOptions webSearchOptions,

            //qwen 3
            @JsonProperty("enable_thinking") Boolean enableThinking
    ) {

        /**
         * Shortcut constructor for a chat completion request with the given messages, model and temperature.
         *
         * @param messages    A list of messages comprising the conversation so far.
         * @param model       ID of the model to use.
         * @param temperature What sampling temperature to use, between 0 and 1.
         */
        public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Double temperature) {
            this(messages, model, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, false, null, temperature, null,
                    null, null, null, null, null, null, false);
        }

        /**
         * Shortcut constructor for a chat completion request with text and audio output.
         *
         * @param messages A list of messages comprising the conversation so far.
         * @param model    ID of the model to use.
         * @param audio    Parameters for audio output. Required when audio output is requested with outputModalities: ["audio"].
         */
        public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, AudioParameters audio, boolean stream) {
            this(messages, model, null, null, null, null, null, null,
                    null, null, null, List.of(OutputModality.AUDIO, OutputModality.TEXT), audio, null, null,
                    null, null, null, stream, null, null, null,
                    null, null, null, null, null, null, false);
        }

        /**
         * Shortcut constructor for a chat completion request with the given messages, model, temperature and control for streaming.
         *
         * @param messages    A list of messages comprising the conversation so far.
         * @param model       ID of the model to use.
         * @param temperature What sampling temperature to use, between 0 and 1.
         * @param stream      If set, partial message deltas will be sent.Tokens will be sent as data-only server-sent events
         *                    as they become available, with the stream terminated by a data: [DONE] message.
         */
        public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Double temperature, boolean stream) {
            this(messages, model, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, stream, null, temperature, null,
                    null, null, null, null, null, null, false);
        }

        /**
         * Shortcut constructor for a chat completion request with the given messages, model, tools and tool choice.
         * Streaming is set to false, temperature to 0.8 and all other parameters are null.
         *
         * @param messages   A list of messages comprising the conversation so far.
         * @param model      ID of the model to use.
         * @param tools      A list of tools the model may call. Currently, only functions are supported as a tool.
         * @param toolChoice Controls which (if any) function is called by the model.
         */
        public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model,
                                     List<FunctionTool> tools, Object toolChoice) {
            this(messages, model, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, false, null, 0.8, null,
                    tools, toolChoice, null, null, null, null, false);
        }

        /**
         * Shortcut constructor for a chat completion request with the given messages for streaming.
         *
         * @param messages A list of messages comprising the conversation so far.
         * @param stream   If set, partial message deltas will be sent.Tokens will be sent as data-only server-sent events
         *                 as they become available, with the stream terminated by a data: [DONE] message.
         */
        public ChatCompletionRequest(List<ChatCompletionMessage> messages, Boolean stream) {
            this(messages, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, stream, null, null, null,
                    null, null, null, null, null, null, false);
        }

        /**
         * Sets the {@link StreamOptions} for this request.
         *
         * @param streamOptions The new stream options to use.
         * @return A new {@link ChatCompletionRequest} with the specified stream options.
         */
        public ChatCompletionRequest streamOptions(StreamOptions streamOptions) {
            return new ChatCompletionRequest(this.messages, this.model, this.store, this.metadata, this.frequencyPenalty, this.logitBias, this.logprobs,
                    this.topLogprobs, this.maxTokens, this.maxCompletionTokens, this.n, this.outputModalities, this.audioParameters, this.presencePenalty,
                    this.responseFormat, this.seed, this.serviceTier, this.stop, this.stream, streamOptions, this.temperature, this.topP,
                    this.tools, this.toolChoice, this.parallelToolCalls, this.user, this.reasoningEffort, this.webSearchOptions,this.enableThinking);
        }

        /**
         * Helper factory that creates a tool_choice of type 'none', 'auto' or selected function by name.
         */
        public static class ToolChoiceBuilder {
            /**
             * Model can pick between generating a message or calling a function.
             */
            public static final String AUTO = "auto";
            /**
             * Model will not call a function and instead generates a message
             */
            public static final String NONE = "none";

            /**
             * Specifying a particular function forces the model to call that function.
             */
            public static Object FUNCTION(String functionName) {
                return Map.of("type", "function", "function", Map.of("name", functionName));
            }
        }


        /**
         * Parameters for audio output. Required when audio output is requested with outputModalities: ["audio"].
         *
         * @param voice  Specifies the voice type.
         * @param format Specifies the output audio format.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record AudioParameters(
                @JsonProperty("voice") Voice voice,
                @JsonProperty("format") AudioResponseFormat format) {

            /**
             * Specifies the voice type.
             */
            public enum Voice {
                /**
                 * Alloy voice
                 */
                @JsonProperty("alloy") ALLOY,
                /**
                 * Echo voice
                 */
                @JsonProperty("echo") ECHO,
                /**
                 * Fable voice
                 */
                @JsonProperty("fable") FABLE,
                /**
                 * Onyx voice
                 */
                @JsonProperty("onyx") ONYX,
                /**
                 * Nova voice
                 */
                @JsonProperty("nova") NOVA,
                /**
                 * Shimmer voice
                 */
                @JsonProperty("shimmer") SHIMMER
            }

            /**
             * Specifies the output audio format.
             */
            public enum AudioResponseFormat {
                /**
                 * MP3 format
                 */
                @JsonProperty("mp3") MP3,
                /**
                 * FLAC format
                 */
                @JsonProperty("flac") FLAC,
                /**
                 * OPUS format
                 */
                @JsonProperty("opus") OPUS,
                /**
                 * PCM16 format
                 */
                @JsonProperty("pcm16") PCM16,
                /**
                 * WAV format
                 */
                @JsonProperty("wav") WAV
            }
        }

        /**
         * @param includeUsage If set, an additional chunk will be streamed
         *                     before the data: [DONE] message. The usage field on this chunk
         *                     shows the token usage statistics for the entire request, and
         *                     the choices field will always be an empty array. All other chunks
         *                     will also include a usage field, but with a null value.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record StreamOptions(
                @JsonProperty("include_usage") Boolean includeUsage) {

            public static StreamOptions INCLUDE_USAGE = new StreamOptions(true);
        }

        /**
         * This tool searches the web for relevant results to use in a response.
         *
         * @param searchContextSize
         * @param userLocation
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record WebSearchOptions(
                @JsonProperty("search_context_size") SearchContextSize searchContextSize,
                @JsonProperty("user_location") UserLocation userLocation) {

            /**
             * High level guidance for the amount of context window space to use for the
             * search. One of low, medium, or high. medium is the default.
             */
            public enum SearchContextSize {

                /**
                 * Low context size.
                 */
                @JsonProperty("low")
                LOW,

                /**
                 * Medium context size. This is the default.
                 */
                @JsonProperty("medium")
                MEDIUM,

                /**
                 * High context size.
                 */
                @JsonProperty("high")
                HIGH

            }

            /**
             * Approximate location parameters for the search.
             *
             * @param type        The type of location approximation. Always "approximate".
             * @param approximate The approximate location details.
             */
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public record UserLocation(@JsonProperty("type") String type,
                                       @JsonProperty("approximate") Approximate approximate) {

                @JsonInclude(JsonInclude.Include.NON_NULL)
                public record Approximate(@JsonProperty("city") String city, @JsonProperty("country") String country,
                                          @JsonProperty("region") String region,
                                          @JsonProperty("timezone") String timezone) {
                }
            }

        }

    }

    /**
     * Message comprising the conversation.
     *
     * @param rawContent  The contents of the message. Can be either a {@link MediaContent}
     *                    or a {@link String}. The response message content is always a {@link String}.
     * @param role        The role of the messages author. Could be one of the {@link Role}
     *                    types.
     * @param name        An optional name for the participant. Provides the model information to
     *                    differentiate between participants of the same role. In case of Function calling,
     *                    the name is the function name that the message is responding to.
     * @param toolCallId  Tool call that this message is responding to. Only applicable for
     *                    the {@link Role#TOOL} role and null otherwise.
     * @param toolCalls   The tool calls generated by the model, such as function calls.
     *                    Applicable only for {@link Role#ASSISTANT} role and null otherwise.
     * @param refusal     The refusal message by the assistant. Applicable only for
     *                    {@link Role#ASSISTANT} role and null otherwise.
     * @param audioOutput Audio response from the model.
     * @param annotations Annotations for the message, when applicable, as when using the
     *                    web search tool.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatCompletionMessage(
            @JsonProperty("content") Object rawContent,
            @JsonProperty("role") Role role,
            @JsonProperty("name") String name,
            @JsonProperty("tool_call_id") String toolCallId,
            @JsonProperty("tool_calls") @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) List<ToolCall> toolCalls,
            @JsonProperty("refusal") String refusal,
            @JsonProperty("audio") AudioOutput audioOutput,
            @JsonProperty("annotations") List<Annotation> annotations,

            /** qwen 思考结果 */
            @JsonProperty("reasoning_content") String reasoningContent
    ) {

        /**
         * Create a chat completion message with the given content and role. All other
         * fields are null.
         *
         * @param content The contents of the message.
         * @param role    The role of the author of this message.
         */
        public ChatCompletionMessage(Object content, Role role) {
            this(content, role, null, null, null, null, null, null,null);
        }

        /**
         * Get message content as String.
         */
        public String content() {
            if (this.rawContent == null) {
                return null;
            }
            if (this.rawContent instanceof String text) {
                return text;
            }
            throw new IllegalStateException("The content is not a string!");
        }

        /**
         * The role of the author of this message.
         */
        public enum Role {

            /**
             * System message.
             */
            @JsonProperty("system")
            SYSTEM,
            /**
             * User message.
             */
            @JsonProperty("user")
            USER,
            /**
             * Assistant message.
             */
            @JsonProperty("assistant")
            ASSISTANT,
            /**
             * Tool message.
             */
            @JsonProperty("tool")
            TOOL

        }

        /**
         * An array of content parts with a defined type. Each MediaContent can be of
         * either "text", "image_url", or "input_audio" type. Only one option allowed.
         *
         * @param type       Content type, each can be of type text or image_url.
         * @param text       The text content of the message.
         * @param imageUrl   The image content of the message. You can pass multiple images
         *                   by adding multiple image_url content parts. Image input is only supported when
         *                   using the gpt-4-visual-preview model.
         * @param inputAudio Audio content part.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record MediaContent(
                @JsonProperty("type") String type,
                @JsonProperty("text") String text,
                @JsonProperty("image_url") ImageUrl imageUrl,
                @JsonProperty("input_audio") InputAudio inputAudio) {

            /**
             * Shortcut constructor for a text content.
             *
             * @param text The text content of the message.
             */
            public MediaContent(String text) {
                this("text", text, null, null);
            }

            /**
             * Shortcut constructor for an image content.
             *
             * @param imageUrl The image content of the message.
             */
            public MediaContent(ImageUrl imageUrl) {
                this("image_url", null, imageUrl, null);
            }

            /**
             * Shortcut constructor for an audio content.
             *
             * @param inputAudio The audio content of the message.
             */
            public MediaContent(InputAudio inputAudio) {
                this("input_audio", null, null, inputAudio);
            }

            /**
             * @param data   Base64 encoded audio data.
             * @param format The format of the encoded audio data. Currently supports
             *               "wav" and "mp3".
             */
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public record InputAudio(// @formatter:off
            @JsonProperty("data") String data,
            @JsonProperty("format") Format format) {

            public enum Format {
                /** MP3 audio format */
                @JsonProperty("mp3") MP3,
                /** WAV audio format */
                @JsonProperty("wav") WAV
            } // @formatter:on
            }

            /**
             * Shortcut constructor for an image content.
             *
             * @param url    Either a URL of the image or the base64 encoded image data. The
             *               base64 encoded image data must have a special prefix in the following
             *               format: "data:{mimetype};base64,{base64-encoded-image-data}".
             * @param detail Specifies the detail level of the image.
             */
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public record ImageUrl(@JsonProperty("url") String url, @JsonProperty("detail") String detail) {

                public ImageUrl(String url) {
                    this(url, null);
                }

            }

        }


        /**
         * The relevant tool call.
         *
         * @param index    The index of the tool call in the list of tool calls. Required in
         *                 case of streaming.
         * @param id       The ID of the tool call. This ID must be referenced when you submit
         *                 the tool outputs in using the Submit tool outputs to run endpoint.
         * @param type     The type of tool call the output is required for. For now, this is
         *                 always function.
         * @param function The function definition.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ToolCall(
                @JsonProperty("index") Integer index,
                @JsonProperty("id") String id,
                @JsonProperty("type") String type,
                @JsonProperty("function") ChatCompletionFunction function) {

            public ToolCall(String id, String type, ChatCompletionFunction function) {
                this(null, id, type, function);
            }

        }


        /**
         * The function definition.
         *
         * @param name      The name of the function.
         * @param arguments The arguments that the model expects you to pass to the
         *                  function.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ChatCompletionFunction(
                @JsonProperty("name") String name,
                @JsonProperty("arguments") String arguments) {
        }

        /**
         * Audio response from the model.
         *
         * @param id         Unique identifier for the audio response from the model.
         * @param data       Audio output from the model.
         * @param expiresAt  When the audio content will no longer be available on the
         *                   server.
         * @param transcript Transcript of the audio output from the model.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record AudioOutput(
                @JsonProperty("id") String id,
                @JsonProperty("data") String data,
                @JsonProperty("expires_at") Long expiresAt,
                @JsonProperty("transcript") String transcript
        ) {
        }

        /**
         * Represents an annotation within a message, specifically for URL citations.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Annotation(
                @JsonProperty("type") String type,
                @JsonProperty("url_citation") UrlCitation urlCitation) {
            /**
             * A URL citation when using web search.
             *
             * @param endIndex   The index of the last character of the URL citation in the
             *                   message.
             * @param startIndex The index of the first character of the URL citation in
             *                   the message.
             * @param title      The title of the web resource.
             * @param url        The URL of the web resource.
             */
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public record UrlCitation(
                    @JsonProperty("end_index") Integer endIndex,
                    @JsonProperty("start_index") Integer startIndex,
                    @JsonProperty("title") String title,
                    @JsonProperty("url") String url) {
            }
        }
    }


    /**
     * Represents a chat completion response returned by model, based on the provided
     * input.
     *
     * @param id                A unique identifier for the chat completion.
     * @param choices           A list of chat completion choices. Can be more than one if n is
     *                          greater than 1.
     * @param created           The Unix timestamp (in seconds) of when the chat completion was
     *                          created.
     * @param model             The model used for the chat completion.
     * @param serviceTier       The service tier used for processing the request. This field is
     *                          only included if the service_tier parameter is specified in the request.
     * @param systemFingerprint This fingerprint represents the backend configuration that
     *                          the model runs with. Can be used in conjunction with the seed request parameter to
     *                          understand when backend changes have been made that might impact determinism.
     * @param object            The object type, which is always chat.completion.
     * @param usage             Usage statistics for the completion request.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatCompletion(// @formatter:off
        @JsonProperty("id") String id,
        @JsonProperty("choices") List<Choice> choices,
        @JsonProperty("created") Long created,
        @JsonProperty("model") String model,
        @JsonProperty("service_tier") String serviceTier,
        @JsonProperty("system_fingerprint") String systemFingerprint,
        @JsonProperty("object") String object,
        @JsonProperty("usage") Usage usage
    ) { // @formatter:on

        /**
         * Chat completion choice.
         *
         * @param finishReason The reason the model stopped generating tokens.
         * @param index        The index of the choice in the list of choices.
         * @param message      A chat completion message generated by the model.
         * @param logprobs     Log probability information for the choice.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Choice(// @formatter:off
            @JsonProperty("finish_reason") ChatCompletionFinishReason finishReason,
            @JsonProperty("index") Integer index,
            @JsonProperty("message") ChatCompletionMessage message,
            @JsonProperty("logprobs") LogProbs logprobs) { // @formatter:on
        }

    }

    /**
     * Log probability information for the choice.
     *
     * @param content A list of message content tokens with log probability information.
     * @param refusal A list of message refusal tokens with log probability information.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LogProbs(
        @JsonProperty("content") List<Content> content,
        @JsonProperty("refusal") List<Content> refusal) {

        /**
         * Message content tokens with log probability information.
         *
         * @param token       The token.
         * @param logprob     The log probability of the token.
         * @param probBytes   A list of integers representing the UTF-8 bytes representation
         *                    of the token. Useful in instances where characters are represented by multiple
         *                    tokens and their byte representations must be combined to generate the correct
         *                    text representation. Can be null if there is no bytes representation for the
         *                    token.
         * @param topLogprobs List of the most likely tokens and their log probability, at
         *                    this token position. In rare cases, there may be fewer than the number of
         *                    requested top_logprobs returned.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Content(// @formatter:off

            @JsonProperty("token") String token,
            @JsonProperty("logprob") Float logprob,
            @JsonProperty("bytes") List<Integer> probBytes,
            @JsonProperty("top_logprobs") List<TopLogProbs> topLogprobs) { // @formatter:on

            /**
             * The most likely tokens and their log probability, at this token position.
             *
             * @param token     The token.
             * @param logprob   The log probability of the token.
             * @param probBytes A list of integers representing the UTF-8 bytes
             *                  representation of the token. Useful in instances where characters are
             *                  represented by multiple tokens and their byte representations must be
             *                  combined to generate the correct text representation. Can be null if there
             *                  is no bytes representation for the token.
             */
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record TopLogProbs(// @formatter:off
                @JsonProperty("token") String token,
                @JsonProperty("logprob") Float logprob,
                @JsonProperty("bytes") List<Integer> probBytes) { // @formatter:on
            }

        }

    }

    // Embeddings API

    /**
     * Usage statistics for the completion request.
     *
     * @param completionTokens       Number of tokens in the generated completion. Only
     *                               applicable for completion requests.
     * @param promptTokens           Number of tokens in the prompt.
     * @param totalTokens            Total number of tokens used in the request (prompt +
     *                               completion).
     * @param promptTokensDetails    Breakdown of tokens used in the prompt.
     * @param completionTokenDetails Breakdown of tokens used in a completion.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(// @formatter:off
            @JsonProperty("completion_tokens") Integer completionTokens,
            @JsonProperty("prompt_tokens") Integer promptTokens,
            @JsonProperty("total_tokens") Integer totalTokens,
            @JsonProperty("prompt_tokens_details") PromptTokensDetails promptTokensDetails,
            @JsonProperty("completion_tokens_details") CompletionTokenDetails completionTokenDetails
    ) { // @formatter:on

        public Usage(Integer completionTokens, Integer promptTokens, Integer totalTokens) {
            this(completionTokens, promptTokens, totalTokens, null, null);
        }

        /**
         * Breakdown of tokens used in the prompt
         *
         * @param audioTokens  Audio input tokens present in the prompt.
         * @param cachedTokens Cached tokens present in the prompt.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record PromptTokensDetails(// @formatter:off
            @JsonProperty("audio_tokens") Integer audioTokens,
            @JsonProperty("cached_tokens") Integer cachedTokens) { // @formatter:on
        }

        /**
         * Breakdown of tokens used in a completion.
         *
         * @param reasoningTokens          Number of tokens generated by the model for reasoning.
         * @param acceptedPredictionTokens Number of tokens generated by the model for
         *                                 accepted predictions.
         * @param audioTokens              Number of tokens generated by the model for audio.
         * @param rejectedPredictionTokens Number of tokens generated by the model for
         *                                 rejected predictions.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record CompletionTokenDetails(// @formatter:off
            @JsonProperty("reasoning_tokens") Integer reasoningTokens,
            @JsonProperty("accepted_prediction_tokens") Integer acceptedPredictionTokens,
            @JsonProperty("audio_tokens") Integer audioTokens,
            @JsonProperty("rejected_prediction_tokens") Integer rejectedPredictionTokens) { // @formatter:on
        }
    }

    /**
     * Represents a streamed chunk of a chat completion response returned by model, based
     * on the provided input.
     *
     * @param id                A unique identifier for the chat completion. Each chunk has the same ID.
     * @param choices           A list of chat completion choices. Can be more than one if n is
     *                          greater than 1.
     * @param created           The Unix timestamp (in seconds) of when the chat completion was
     *                          created. Each chunk has the same timestamp.
     * @param model             The model used for the chat completion.
     * @param serviceTier       The service tier used for processing the request. This field is
     *                          only included if the service_tier parameter is specified in the request.
     * @param systemFingerprint This fingerprint represents the backend configuration that
     *                          the model runs with. Can be used in conjunction with the seed request parameter to
     *                          understand when backend changes have been made that might impact determinism.
     * @param object            The object type, which is always 'chat.completion.chunk'.
     * @param usage             Usage statistics for the completion request. Present in the last chunk
     *                          only if the StreamOptions.includeUsage is set to true.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatCompletionChunk(// @formatter:off
            @JsonProperty("id") String id,
            @JsonProperty("choices") List<ChunkChoice> choices,
            @JsonProperty("created") Long created,
            @JsonProperty("model") String model,
            @JsonProperty("service_tier") String serviceTier,
            @JsonProperty("system_fingerprint") String systemFingerprint,
            @JsonProperty("object") String object,
            @JsonProperty("usage") Usage usage) { // @formatter:on

        /**
         * Chat completion choice.
         *
         * @param finishReason The reason the model stopped generating tokens.
         * @param index        The index of the choice in the list of choices.
         * @param delta        A chat completion delta generated by streamed model responses.
         * @param logprobs     Log probability information for the choice.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ChunkChoice(// @formatter:off
            @JsonProperty("finish_reason") ChatCompletionFinishReason finishReason,
            @JsonProperty("index") Integer index,
            @JsonProperty("delta") ChatCompletionMessage delta,
            @JsonProperty("logprobs") LogProbs logprobs) { // @formatter:on

        }

    }

    /**
     * Represents an embedding vector returned by embedding endpoint.
     *
     * @param index     The index of the embedding in the list of embeddings.
     * @param embedding The embedding vector, which is a list of floats. The length of
     *                  vector depends on the model.
     * @param object    The object type, which is always 'embedding'.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Embedding(// @formatter:off
        @JsonProperty("index") Integer index,
        @JsonProperty("embedding") float[] embedding,
        @JsonProperty("object") String object) { // @formatter:on

        /**
         * Create an embedding with the given index, embedding and object type set to
         * 'embedding'.
         *
         * @param index     The index of the embedding in the list of embeddings.
         * @param embedding The embedding vector, which is a list of floats. The length of
         *                  vector depends on the model.
         */
        public Embedding(Integer index, float[] embedding) {
            this(index, embedding, "embedding");
        }

    }

    /**
     * Creates an embedding vector representing the input text.
     *
     * @param <T>            Type of the input.
     * @param input          Input text to embed, encoded as a string or array of tokens. To embed
     *                       multiple inputs in a single request, pass an array of strings or array of token
     *                       arrays. The input must not exceed the max input tokens for the model (8192 tokens
     *                       for text-embedding-ada-002), cannot be an empty string, and any array must be 2048
     *                       dimensions or less.
     * @param model          ID of the model to use.
     * @param encodingFormat The format to return the embeddings in. Can be either float
     *                       or base64.
     * @param dimensions     The number of dimensions the resulting output embeddings should
     *                       have. Only supported in text-embedding-3 and later models.
     * @param user           A unique identifier representing your end-user, which can help OpenAI
     *                       to monitor and detect abuse.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EmbeddingRequest<T>(// @formatter:off
        @JsonProperty("input") T input,
        @JsonProperty("model") String model,
        @JsonProperty("encoding_format") String encodingFormat,
        @JsonProperty("dimensions") Integer dimensions,
        @JsonProperty("user") String user) { // @formatter:on

        /**
         * Create an embedding request with the given input, model and encoding format set
         * to float.
         *
         * @param input Input text to embed.
         * @param model ID of the model to use.
         */
        public EmbeddingRequest(T input, String model) {
            this(input, model, "float", null, null);
        }

        /**
         * Create an embedding request with the given input. Encoding format is set to
         * float and user is null and the model is set to 'text-embedding-ada-002'.
         *
         * @param input Input text to embed.
         */
        public EmbeddingRequest(T input) {
            this(input, DEFAULT_EMBEDDING_MODEL);
        }

    }

    /**
     * List of multiple embedding responses.
     *
     * @param <T>    Type of the entities in the data list.
     * @param object Must have value "list".
     * @param data   List of entities.
     * @param model  ID of the model to use.
     * @param usage  Usage statistics for the completion request.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmbeddingList<T>(// @formatter:off
        @JsonProperty("object") String object,
        @JsonProperty("data") List<T> data,
        @JsonProperty("model") String model,
        @JsonProperty("usage") Usage usage) { // @formatter:on
    }

    public static class Builder {

        public Builder() {
        }

        // Copy constructor for mutate()
        public Builder(AiCommonApi api) {
            this.baseUrl = api.getBaseUrl();
            this.apiKey = api.getApiKey();
            this.headers = new LinkedMultiValueMap<>(api.getHeaders());
            this.completionsPath = api.getCompletionsPath();
            this.embeddingsPath = api.getEmbeddingsPath();
            this.restClientBuilder = api.restClient != null ? api.restClient.mutate() : RestClient.builder();
            this.webClientBuilder = api.webClient != null ? api.webClient.mutate() : WebClient.builder();
            this.responseErrorHandler = api.getResponseErrorHandler();
        }

        private String baseUrl = OpenAiApiConstants.DEFAULT_BASE_URL;

        private ApiKey apiKey;

        private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        private String completionsPath = "/v1/chat/completions";

        private String embeddingsPath = "/v1/embeddings";

        private RestClient.Builder restClientBuilder = RestClient.builder();

        private WebClient.Builder webClientBuilder = WebClient.builder();

        private ResponseErrorHandler responseErrorHandler = RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER;

        public Builder baseUrl(String baseUrl) {
            Assert.hasText(baseUrl, "baseUrl cannot be null or empty");
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder apiKey(ApiKey apiKey) {
            Assert.notNull(apiKey, "apiKey cannot be null");
            this.apiKey = apiKey;
            return this;
        }

        public Builder apiKey(String simpleApiKey) {
            Assert.notNull(simpleApiKey, "simpleApiKey cannot be null");
            this.apiKey = new SimpleApiKey(simpleApiKey);
            return this;
        }

        public Builder headers(MultiValueMap<String, String> headers) {
            Assert.notNull(headers, "headers cannot be null");
            this.headers = headers;
            return this;
        }

        public Builder completionsPath(String completionsPath) {
            Assert.hasText(completionsPath, "completionsPath cannot be null or empty");
            this.completionsPath = completionsPath;
            return this;
        }

        public Builder embeddingsPath(String embeddingsPath) {
            Assert.hasText(embeddingsPath, "embeddingsPath cannot be null or empty");
            this.embeddingsPath = embeddingsPath;
            return this;
        }

        public Builder restClientBuilder(RestClient.Builder restClientBuilder) {
            Assert.notNull(restClientBuilder, "restClientBuilder cannot be null");
            this.restClientBuilder = restClientBuilder;
            return this;
        }

        public Builder webClientBuilder(WebClient.Builder webClientBuilder) {
            Assert.notNull(webClientBuilder, "webClientBuilder cannot be null");
            this.webClientBuilder = webClientBuilder;
            return this;
        }

        public Builder responseErrorHandler(ResponseErrorHandler responseErrorHandler) {
            Assert.notNull(responseErrorHandler, "responseErrorHandler cannot be null");
            this.responseErrorHandler = responseErrorHandler;
            return this;
        }

        public AiCommonApi build() {
            Assert.notNull(this.apiKey, "apiKey must be set");
            return new AiCommonApi(this.baseUrl, this.apiKey, this.headers, this.completionsPath, this.embeddingsPath,
                    this.restClientBuilder, this.webClientBuilder, this.responseErrorHandler);
        }

    }
}

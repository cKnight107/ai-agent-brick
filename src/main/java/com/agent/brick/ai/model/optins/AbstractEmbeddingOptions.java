package com.agent.brick.ai.model.optins;

import com.agent.brick.ai.model.enums.ChatModelEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.ai.embedding.EmbeddingOptions;

/**
 * @since 2025/6/27
 *
 * @author cKnight
 */
@Data
public abstract class AbstractEmbeddingOptions implements EmbeddingOptions {

    /**
     * ID of the model to use.
     */
    private @JsonProperty("model") String model;
    /**
     * The format to return the embeddings in. Can be either float or base64.
     */
    private @JsonProperty("encoding_format") String encodingFormat;
    /**
     * The number of dimensions the resulting output embeddings should have. Only supported in text-embedding-3 and later models.
     */
    private @JsonProperty("dimensions") Integer dimensions;
    /**
     * A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
     */
    private @JsonProperty("user") String user;

    public static class Builder<T extends AbstractEmbeddingOptions> {

        private T options;

        public Builder(Class<T> clazz) {
            try {
                this.options = clazz.getDeclaredConstructor().newInstance();
            }catch (Exception e){}
        }

        public Builder<T> model(String model) {
            this.options.setModel(model);
            return this;
        }

        public Builder<T> model(ChatModelEnum model) {
            this.options.setModel(model.value);
            return this;
        }

        public Builder<T> encodingFormat(String encodingFormat) {
            this.options.setEncodingFormat(encodingFormat);
            return this;
        }

        public Builder<T> dimensions(Integer dimensions) {
            this.options.setDimensions(dimensions);
            return this;
        }

        public Builder<T> user(String user) {
            this.options.setUser(user);
            return this;
        }

        public T build() {
            return this.options;
        }

    }
}

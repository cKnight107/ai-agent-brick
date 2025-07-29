package com.agent.brick.ai.tools.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * dify 知识库检索 入参
 * </p>
 *
 * @author cKnight
 * @since 2025/7/17
 */
@Data
public class DifyDatasetReq {
    @JsonProperty("query")
    private String query;

    @JsonProperty("retrieval_model")
    private RetrievalModel retrievalModel;

    public DifyDatasetReq(String query) {
        this.query = query;
        this.retrievalModel = new RetrievalModel(2);
    }

    public DifyDatasetReq(String query,Integer topK,List<String> documentNames) {
        if (Objects.isNull(topK) || topK <= 0) {
            topK = 2;
        }
        this.query = query;
        this.retrievalModel = new RetrievalModel(topK,documentNames);
    }


    @AllArgsConstructor
    @NoArgsConstructor
    public class RetrievalModel {
        public RetrievalModel(Integer topK){
            this.topK = topK;
        }

        public RetrievalModel(Integer topK,List<String> documentNames) {
            this.topK = topK;
            this.metadataFilteringConditions = CollectionUtils.isEmpty(documentNames) ? null :
                    new MetadataFilteringConditions(
                            documentNames.stream().map(Conditions::new).collect(Collectors.toList())
                    );
        }
        @JsonProperty(value = "search_method")
        private String searchMethod = "hybrid_search";

        @JsonProperty(value = "reranking_enable")
        private boolean rerankingEnable = true;

        @JsonProperty("reranking_model")
        private RerankingMode rerankingModel = new RerankingMode();

        @JsonProperty("top_k")
        private Integer topK;

        @JsonProperty("score_threshold_enabled")
        private boolean scoreThresholdEnabled;

        /** 元数据条件过滤 */
        @JsonProperty("metadata_filtering_conditions")
        private MetadataFilteringConditions metadataFilteringConditions;
    }

    @NoArgsConstructor
    public class RerankingMode {
        @JsonProperty("rerankingProviderName")
        private String rerankingProviderName = "langgenius/tongyi/tongyi";

        @JsonProperty("reranking_model_name")
        private String rerankingModelName = "gte-rerank-v2";
    }


    @NoArgsConstructor
    public class MetadataFilteringConditions {
        /** 逻辑运算符 and | or */
        @JsonProperty("logical_operator")
        private String logicalOperator = "or";
        /** 条件 */
        @JsonProperty("conditions")
        private List<Conditions> conditions;

        public MetadataFilteringConditions(List<Conditions> conditions) {
            this.conditions = conditions;
        }
    }

    @NoArgsConstructor
    public class Conditions{

        @JsonProperty("name")
        private String name = "document_name";

        @JsonProperty("comparison_operator")
        private String comparisonOperator = "contains";

        @JsonProperty("value")
        private String value;

        public Conditions(String value){
            this.value = value;
        }
    }
}

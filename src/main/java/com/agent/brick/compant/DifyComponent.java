package com.agent.brick.compant;

import com.agent.brick.ai.tools.request.DifyDatasetReq;
import com.agent.brick.config.DifyConfig;
import com.agent.brick.enums.OpenApiEnum;
import com.agent.brick.util.JSONUtils;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p>
 * Dify 组件
 * </p>
 *
 * @author cKnight
 * @since 2025/7/18
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DifyComponent {
    private final DifyConfig difyConfig;

    private final HttpHeaders difyHeaders;

    private final RestHttpComponent restHttpComponent;

    /**
     * 知识库检索
     * @param req 请求数据
     * @return JSON
     */
    public JSONObject datasetRetrieve(DifyDatasetReq req){
        return datasetRetrieve(difyConfig.getDatasetId(),req);
    }

    public JSONObject datasetRetrieve(String datasetId,DifyDatasetReq req){
        String url = OpenApiEnum.DIFY_RETRIEVE.getUrl(difyConfig.getBaseUrl(), datasetId);
        return restHttpComponent.postBodyForJSON(url, JSONUtils.parseObj(req), difyHeaders);
    }

    public JSONObject datasetDocuments(){
        return datasetDocuments("");
    }

    public JSONObject datasetDocuments(String documentName){
        return datasetDocuments(difyConfig.getDatasetId(),documentName);
    }

    public JSONObject datasetDocuments(String datasetId,String documentName){
        return datasetDocuments(datasetId,documentName,100,1);
    }

    /**
     * 获取知识库内文档列表
     * @param documentName 文档名称
     * @return JSON
     */
    public JSONObject datasetDocuments(String datasetId,String documentName,Integer pageSize,Integer pageNum){
        String url = OpenApiEnum.DIFY_DOCUMENTS.getUrl(difyConfig.getBaseUrl(), datasetId);
        Map<String, Object> params = Map.of("keyword", documentName, "page", String.valueOf(pageNum), "limit", String.valueOf(pageSize));
        return restHttpComponent.getForJSON(url, params, difyHeaders);
    }
}

package com.agent.brick.api;

import com.agent.brick.ai.tools.request.DifyDatasetReq;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Dify 接口
 * </p>
 *
 * @author cKnight
 * @since 2025/11/13
 */
@HttpExchange
public interface DifyHttpClient {

    /**
     * 获取知识库内文档列表
     *
     * @param datasetId 数据集id
     * @param keyword   关键词
     * @param page      页数
     * @param limit     尺寸
     * @return res
     */
    @GetExchange("/datasets/{datasetId}/retrieve")
    JSONObject datasetDocuments(@PathVariable String datasetId,
                                @RequestParam(defaultValue = "") String keyword,
                                @RequestParam(required = false, defaultValue = "1") Integer page,
                                @RequestParam(required = false, defaultValue = "100") Integer limit);

    @GetExchange("/datasets/{datasetId}/retrieve")
    JSONObject datasetDocuments(@PathVariable String datasetId,
                                @RequestParam(required = false, defaultValue = "1") Integer page,
                                @RequestParam(required = false, defaultValue = "100") Integer limit);


    default List<String> datasetDocuments(String datasetId){
        return this.datasetDocuments(datasetId,null);
    }

    default List<String> datasetDocuments(String datasetId, String keywork) {
        JSONObject res = StringUtils.isEmpty(keywork) ?
                this.datasetDocuments(datasetId, 1, 100) :
                this.datasetDocuments(datasetId, keywork, 1, 100);
        if (Objects.isNull(res) || Objects.isNull(res.get("data"))) {
            return List.of();
        }
        return res.getJSONArray("data").stream().map(obj -> {
            JSONObject objJson = (JSONObject) obj;
            return objJson.getString("name");
        }).toList();
    }

    /**
     * 检索知识库
     * @param datasetId 数据集id
     * @param req 请求
     * @return res
     */
    @GetExchange("/datasets/{datasetId}/retrieve")
    JSONObject datasetRetrieve(@PathVariable String datasetId, @RequestBody DifyDatasetReq req);


}

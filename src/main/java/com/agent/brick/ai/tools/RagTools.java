package com.agent.brick.ai.tools;

import com.agent.brick.ai.tools.request.DifyDatasetReq;
import com.agent.brick.compant.DifyComponent;
import com.agent.brick.util.JSONUtils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * RAG 工具 LLM调用
 * </p>
 *
 * @author cKnight
 * @since 2025/7/18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagTools {

    private final DifyComponent difyComponent;

    @Tool(description = "检索知识库，返回JSON对象信息")
    public JSONObject retrieve(
            @ToolParam(description = "需要检索的问题或关键词") String query,
            @ToolParam(description = "返回结果数量",required = false) Integer topK,
            @ToolParam(description = "存在的文档名称列表",required = false) List<String> documentNames
    ){
        log.info("执行检索知识库,开始检索dify,query:{},topK:{},documentName:{}",query,topK,documentNames);
        DifyDatasetReq difyDatasetReq = new DifyDatasetReq(query, topK,documentNames);
        JSONObject res = difyComponent.datasetRetrieve(difyDatasetReq);
        JSONArray records = res.getJSONArray("records");
        List<JSONObject> data = records.stream().map(obj -> {
            JSONObject object = (JSONObject) obj;
            JSONObject segment = object.getJSONObject("segment");
            JSONObject document = segment.getJSONObject("document");
            return JSONUtils.builder().put("content", segment.getString("content")).put("document_name", document.getString("name")).build();
        }).toList();
        return JSONUtils.builder().put("data",data).build();
    }

    @Tool(description = "获取所有的文档列表")
    public JSONObject documents(){
        JSONObject res = difyComponent.datasetDocuments();
        List<String> data = res.getJSONArray("data").stream().map(obj -> {
            JSONObject objJson = (JSONObject) obj;
            return objJson.getString("name");
        }).toList();
        return JSONUtils.builder().put("data",data).build();
    }
}

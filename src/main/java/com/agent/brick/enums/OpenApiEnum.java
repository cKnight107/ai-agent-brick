package com.agent.brick.enums;

import com.agent.brick.util.AiUtil;
import lombok.AllArgsConstructor;

import java.util.*;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/7/18
 */
@AllArgsConstructor
public enum OpenApiEnum {
    DIFY_RETRIEVE("/datasets/{1}/retrieve","dify检索知识库",HttpMethodEnum.POST),
    DIFY_DOCUMENTS("/datasets/{1}/documents","dify知识库文档列表",HttpMethodEnum.GET),
    ;
    private String uri;
    public String description;
    public HttpMethodEnum  httpMethod;

    /**
     * 获取url
     * @param host host
     * @param args 路径参数
     * @return url
     */
    public String getUrl(String host,Object... args){
        String url = host+this.uri;
        if (Objects.isNull(args)) {
            return url;
        }
        List<Object> argList = Arrays.stream(args).toList();
        Map<String,Object> map = new HashMap<>();
        for (int i = 0; i < argList.size(); i++) {
            map.put(STR."\{i+1}",argList.get(i));
        }
        return AiUtil.strFormat(map,url);
    }
}

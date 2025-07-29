package com.agent.brick.compant;

import com.agent.brick.util.CommonUtils;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

/**
 * 通用 http连接组件
 * @author cKnight
 * @since  2024/6/11
 */
@Component
@Slf4j
public class RestHttpComponent {
    @Resource
    private RestTemplate restTemplate;

    /**
     * 返回对应实体类
     * @param url
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> T getForObject(String url, Class<T> clazz) {
        log.info("RestHttpGet调用,url:{}", url);
        return process(() -> Objects.requireNonNull(restTemplate.getForObject(url, clazz)));
    }

    /**
     * 自动拼装 url参数
     * @param url
     * @param params 可为null
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> T getForObject(String url, Map<String, Object> params, Class<T> clazz) {
        //自动拼装参数
        url = CommonUtils.transUrl(url, params);
        log.info("RestHttpGet调用,params:{}", params);
        return getForObject(url, clazz);
    }

    /**
     * 自动拼装 url参数
     * 返回全信息
     * @param url
     * @param params
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> ResponseEntity<T> getForEntity(String url, Map<String, Object> params, Class<T> clazz) {
        //自动拼装参数
        String finalUrl = CommonUtils.transUrl(url, params);
        log.info("RestHttpGetEntity调用,url:{},params:{}", url, params);
        return process(() -> Objects.requireNonNull(restTemplate.getForEntity(finalUrl, clazz)));
    }


    /**
     * 带有头信息的get请求
     * @param url
     * @param params
     * @param headers
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> ResponseEntity<T> getForObject(String url, Map<String, Object> params, HttpHeaders headers, Class<T> clazz) {
        String finalUrl = CommonUtils.transUrl(url, params);
        log.info("RestHttpGet调用,url:{},params:{},headers:{}", finalUrl, params, headers.toString());
        return process(() -> Objects.requireNonNull(
                restTemplate.exchange(finalUrl, HttpMethod.GET, new HttpEntity<>(headers), clazz)
        ));
    }

    public JSONObject getForJSON(String url) {
        return JSONObject.parseObject(getForObject(url, null, new HttpHeaders(), String.class).getBody());
    }

    public JSONObject getForJSON(String url, HttpHeaders headers) {
        return JSONObject.parseObject(getForObject(url, null, headers, String.class).getBody());
    }

    public JSONObject getForJSON(String url, Map<String, Object> params) {
        return JSONObject.parseObject(getForObject(url, params, new HttpHeaders(), String.class).getBody());
    }

    public JSONObject getForJSON(String url, Map<String, Object> params, HttpHeaders headers) {
        return JSONObject.parseObject(getForObject(url, params, headers, String.class).getBody());
    }

    public <T> ResponseEntity<T> postBodyForEntity(String url, JSONObject params, Class<T> clazz) {
        return postBodyForEntity(url, params, new HttpHeaders(), clazz);
    }

    public <T> ResponseEntity<T> postBodyForEntity(String url, MultiValueMap<String, Object> params, Class<T> clazz) {
        return postBodyForEntity(url, params, new HttpHeaders(), clazz);
    }

    public <T> ResponseEntity<T> postBodyForEntity(String url, Map<String, Object> params, Class<T> clazz) {
        return postBodyForEntity(url, params, new HttpHeaders(), clazz);
    }

    public <T> JSONObject postBodyForJSON(String url, MultiValueMap<String, Object> params, HttpHeaders headers) {
        return JSONObject.parseObject(postBodyForEntity(url, params, headers, String.class).getBody());
    }

    public JSONObject postBodyForJSON(String url, JSONObject params, HttpHeaders headers) {
        return JSONObject.parseObject(postBodyForEntity(url, params, headers, String.class).getBody());
    }

    public JSONObject postBodyForJSON(String url, JSONObject params) {
        return postBodyForJSON(url, params, new HttpHeaders());
    }

    public JSONObject postBodyForJSON(String url) {
        return postBodyForJSON(url, new JSONObject(), new HttpHeaders());
    }

    /**
     * POST 请求
     *
     * @param url           请求路径
     * @param requestEntity 请求参数
     * @param clazz         响应类型
     * @param <T>           响应类型
     * @return 响应实体
     */
    public <T> T postForObject(String url, HttpEntity<MultiValueMap<String, Object>> requestEntity, Class<T> clazz) {
        return restTemplate.postForObject(url, requestEntity, clazz);
    }

    public <T> ResponseEntity<T> postForEntity(String url, HttpEntity<MultiValueMap<String, Object>> requestEntity, Class<T> clazz) {
        return process(() ->
                Objects.requireNonNull(
                        restTemplate.exchange(url
                                , HttpMethod.POST
                                , requestEntity
                                , clazz)));
    }

    /**
     * 带有头信息的post请求 自动填充json信息
     * @param url
     * @param params
     * @param headers
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> ResponseEntity<T> postBodyForEntity(String url, Map<String, Object> params, HttpHeaders headers, Class<T> clazz) {
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        log.info("RestHttpPost调用,url:{},params:{},headers:{}", url, params, headers);
        return process(() ->
                Objects.requireNonNull(
                        restTemplate.exchange(url
                                , HttpMethod.POST
                                , new HttpEntity<>(params, headers)
                                , clazz)));
    }

    /**
     * 表单传递
     * @param url
     * @param params
     * @param headers
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> ResponseEntity<T> postBodyForEntity(String url, MultiValueMap<String, Object> params, HttpHeaders headers, Class<T> clazz) {
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.MULTIPART_FORM_DATA.toString());
        log.info("RestHttpPost调用,url:{},params:{},headers:{}", url, params, headers);
        return process(() ->
                Objects.requireNonNull(
                        restTemplate.exchange(url
                                , HttpMethod.POST
                                , new HttpEntity<>(params, headers)
                                , clazz)));
    }

    /**
     * json 传递
     * @param url
     * @param params
     * @param headers
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> ResponseEntity<T> postBodyForEntity(String url, JSONObject params, HttpHeaders headers, Class<T> clazz) {
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        log.info("RestHttpPost调用,url:{},params:{},headers:{}", url, params, headers);
        return process(() ->
                Objects.requireNonNull(
                        restTemplate.exchange(url
                                , HttpMethod.POST
                                , new HttpEntity<>(params, headers)
                                , clazz)));
    }

    public JSONObject delBodyFroJson(String url, HttpHeaders headers) {
        return JSONObject.parseObject(delBodyForEntity(url, headers, String.class).getBody());
    }

    public <T> ResponseEntity<T> delBodyForEntity(String url, HttpHeaders headers, Class<T> clazz) {
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        log.info("RestHttpPost调用,url:{},headers:{}", url, headers);
        return process(() ->
                Objects.requireNonNull(
                        restTemplate.exchange(url
                                , HttpMethod.DELETE
                                , new HttpEntity<>(headers)
                                , clazz)));
    }


    /**
     * 安全处理
     * @param objectFactory
     * @return
     * @param <T>
     */
    private <T> T process(ObjectFactory<T> objectFactory) {
        try {
            T object = objectFactory.getObject();
            log.info("RestHttp调用,响应结果:{}", object);
            return object;
        } catch (Exception e) {
            log.error("RestHttp调用异常:", e);
            throw e;
        }
    }

    public HttpHeaders getKeyHeader(String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + key);
        return headers;
    }
}

package com.agent.brick.util;

import com.agent.brick.enums.BizCodeEnum;
import com.agent.brick.enums.EventEnums;
import com.agent.brick.exception.BizException;
import com.agent.brick.process.Process;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 公共工具类
 * @author cKnight
 * @since 2025/6/9
 */
@Slf4j
public class CommonUtils {


    /**
     * 校验必填项
     *
     * @param args
     */
    public static void checkArgs(Object... args) {
        for (Object item : args) {
            if (item instanceof String) {
                if (StringUtils.isBlank((String) item)) {
                    throw new BizException(BizCodeEnum.SYS_ARGS_ERROR);
                }
            } else if (item instanceof Collection) {
                if (CollectionUtils.isEmpty((Collection<?>) item)) {
                    throw new BizException(BizCodeEnum.SYS_ARGS_ERROR);
                }
            } else if (Objects.isNull(item)) {
                throw new BizException(BizCodeEnum.SYS_ARGS_ERROR);
            }
        }
    }


    /**
     * 响应json数据给前端
     *
     * @param response
     * @param obj
     */
    public static void sendJsonMessage(HttpServletResponse response, Object obj) {

        response.setContentType("application/json; charset=utf-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.print(JSONObject.toJSONString(obj));
            response.flushBuffer();

        } catch (IOException e) {
            log.warn("响应json数据给前端异常:{}", e.toString());
        }
    }

    /**
     * 转换url
     *
     * @param url
     * @param params
     * @return
     */
    public static String transUrl(String url, Map<String, Object> params) {
        if (MapUtils.isEmpty(params)) {
            return url;
        }
        String transParams = params.entrySet().stream()
                .filter(obj->{
                    if (obj.getValue() instanceof String str) {
                        return StringUtils.isNotBlank(str);
                    }else {
                        return Objects.nonNull(obj.getValue());
                    }
                })
                .map(obj -> STR."\{obj.getKey()}=\{obj.getValue().toString()}")
                .collect(Collectors.joining("&"));
        url = STR."\{url}?\{transParams}";
        return url;
    }


    /**
     * 生成一个的sse对象
     *
     * @param id    id
     * @param event 事件
     * @param msg   信息
     * @return sse
     */
    public static ServerSentEvent<String> genMsg(String id, EventEnums event, String msg) {
        return ServerSentEvent.<String>builder()
                .event(event.name())
                .data(getMsgJson(msg))
                .id(id)
                .build();
    }


    public static ServerSentEvent<String> genMsg(String id, EventEnums event, ChatResponse chatResponse) {
        return ServerSentEvent.<String>builder()
                .event(event.name())
                .data(
                        getMsgJson(
                                chatResponse.getResult().getOutput().getText(),
                                (String) chatResponse.getResult().getOutput()
                                        .getMetadata().getOrDefault("reasoningContent", "")
                        )
                )
                .id(id)
                .build();
    }

    private static String getMsgJson(String msg) {
        return JSONUtils.builder().put("msg", msg).build().toJSONString();
    }

    private static String getMsgJson(String msg, String reasoning) {
        return JSONUtils.builder().put("msg", msg).put("reasoning", reasoning).build().toJSONString();
    }

    public static List<String> getAllFieldName(Class<?> clazz) {
        return getAllFields(clazz).stream().map(obj -> {
            JsonProperty jsonPropertyAnnotation = obj.getAnnotation(JsonProperty.class);
            if (Objects.nonNull(jsonPropertyAnnotation)) {
                return jsonPropertyAnnotation.value();
            } else {
                return obj.getName();
            }
        }).collect(Collectors.toList());
    }

    /**
     * 递归获取所有父类字段
     *
     * @param clazz class
     * @return list  field
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                fields.add(field);
            }
            // 获取父类
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}

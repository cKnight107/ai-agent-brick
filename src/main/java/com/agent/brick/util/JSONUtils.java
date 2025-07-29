package com.agent.brick.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * json工具类
 * @author cKnight
 * @since 2024/8/7
 */
@Slf4j
public class JSONUtils {

    public static JSONObject parseObj(Object object){
        return JSONObject.parseObject(JSONObject.toJSONString(object));
    }

    /**
     * 根据key获取json的值
     * 递归遍历获取值
     * @param json
     * @param key
     * @return
     */
    public static <T> T findValueByKey(JSONObject json, String key, T defaultValue) {
        if (Objects.isNull(json) || Objects.isNull(key)) {
            return defaultValue;
        }
        try {
            // 直接尝试获取对象，如果不存在则继续递归查找
            if (json.containsKey(key)) {
                return (T) json.get(key);
            }
            // 遍历键值对
            for (String currentKey : json.keySet()) {
                Object value = json.get(currentKey);
                if (value instanceof JSONObject) {
                    // 如果是JSONObject，递归查找
                    T valueByKey = findValueByKey((JSONObject) value, key, defaultValue);
                    if (valueByKey != null) {
                        return valueByKey;
                    }
                } else if (value instanceof JSONArray) {
                    // 如果是JSONArray，遍历数组元素
                    for (int i = 0; i < ((JSONArray) value).size(); i++) {
                        Object arrayValue = ((JSONArray) value).get(i);
                        if (arrayValue instanceof JSONObject) {
                            // 如果是JSONObject，递归查找
                            T valueByKey = findValueByKey((JSONObject) arrayValue, key, defaultValue);
                            if (valueByKey != null) {
                                return valueByKey;
                            }
                        }
                    }
                }
            }
        } catch (ClassCastException e) {
            log.error("类型转换错误，无法从json中获取值: {}", key, e);
        } catch (Exception e) {
            log.error("根据key获取json失败: {}", key, e);
        }
        return defaultValue;
    }

    public static JsonObjNode builder(){
        return new JsonObjNode();
    }

    public static List<JSONObject> findJSONArray(JSONObject json,String key) {
        JSONArray jsonArray = json.getJSONArray(key);
        if (Objects.isNull(jsonArray)){
            return null;
        }
       return jsonArray.stream().map(obj->(JSONObject) obj).collect(Collectors.toList());
    }

    public static <T> List<T> findJSONArray(JSONObject json,String key,Class<T> clazz) {
        JSONArray jsonArray = json.getJSONArray(key);
        if (Objects.isNull(jsonArray)){
            return null;
        }
        return jsonArray.stream().map(obj->(T) obj).collect(Collectors.toList());
    }


    public static class JsonObjNode{
        private String name;
        @Getter
        private JSONObject currentObj;
        private JsonObjNode parentObjNode;
        private JsonArrNode parentArrNode;

        public JsonObjNode(){
            this.currentObj = new JSONObject();
        }

        public JsonObjNode(JsonObjNode parentObjNode,String name){
            this.name = name;
            this.currentObj = new JSONObject();
            this.parentObjNode = parentObjNode;
        }

        public JsonObjNode(JsonArrNode arr){
            this.currentObj = new JSONObject();
            this.parentArrNode = arr;
        }

        public JsonObjNode(JsonObjNode obj){
            this.currentObj = new JSONObject();
            this.parentObjNode = obj;
        }

        public JsonObjNode put(String k,Object v){
            this.currentObj.put(k,v);
            return this;
        }

        public JsonObjNode end(){
            this.parentObjNode.put(name,this.currentObj);
            return parentObjNode;
        }

        public JsonArrNode endArr(){
            this.parentArrNode.getCurrentArr().add(this.currentObj);
            return parentArrNode;
        }


        public JsonObjNode putObj(String name){
            return new JsonObjNode(this,name);
        }

        public JsonArrNode putArr(String name){
            return new JsonArrNode(this,name);
        }

        public JSONObject build(){
            return this.currentObj;
        }
    }

    public static class JsonArrNode{
        private String name;
        @Getter
        private JSONArray currentArr;
        private JsonObjNode parentObjNode;

        public JsonArrNode(String name){
            this.name = name;
            this.currentArr = new JSONArray();
        }

        public JsonArrNode(JsonObjNode obj,String name){
            this.currentArr = new JSONArray();
            this.parentObjNode = obj;
            this.name = name;
        }

        public JsonObjNode addObj(){
            return new JsonObjNode(this);
        }

        public JsonObjNode end(){
            this.parentObjNode.put(name,this.currentArr);
            return this.parentObjNode;
        }
    }
}

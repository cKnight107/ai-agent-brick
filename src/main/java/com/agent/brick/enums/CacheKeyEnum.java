package com.agent.brick.enums;

import lombok.Getter;

/**
 * 缓存 key 统一枚举
 * @author chenKai
 * @since 2024/6/11
 * @author chenKai
 */
@Getter
public enum CacheKeyEnum {
    /**
     * 后管
     */
    ADMIN_LOGIN_KEY("admin:login:%s:%s", "后管登录缓存key userId token"),

    AUTH_TOKEN("auth:token:%s", "身份认证令牌 token"),

    ROLE_PER("auth:role:per:%s", "角色权限标识 roleKey"),

    /**
     * LOCK
     */
    LOCK_KEY_PREFIX("Lock::%s", "分布式锁"),
    POWER_LOCK_TOKEN("power_lock:token:%s", "幂等性锁 随机数"),

    /**
     * comfyui
     */
    COMFY_UI_URL_CACHE("comfyUI:url:cache:%s", "comfyUI地址缓存"),
    COMFY_UI_URL_QUEUE_RUNNING("comfyUI:url:queue:%s:running", "comfyUI队列进行中"),
    COMFY_UI_URL_QUEUE_PENDING("comfyUI:url:queue:%s:pending", "comfyUI队列排队中"),
    COMFY_UI_URL_QUEUE_RANKING("comfyUI:url:queue:%s:ranking", "comfyUI队列排队中"),

    /**
     * GPU
     */
    GPU_GET("gpu:dto:%s", "GPU列表"),


    /**
     * ai
     */
    AI_LATERAL_TYPE("AI_LATERAL_TYPE:%s:%s", "三方管理缓存 类型 对象id"),
    AI_GLOBAL_LATERAL_TYPE("AI_GLOBAL_LATERAL_TYPE:%s", "三方管理缓存 类型"),


    AI_MJ_FINISH_TASK_NUM("AI_MJ_FINISH_TASK_NUM:%s", "MJ子任务完成数量 对象id"),
    AI_MJ_TASK_PROGRESS("AI_MJ_TASK_PROGRESS:%s", "外部任务进度 任务id"),

    /**
     * 任务
     */
    TAKS_KEY_PREFIX("TASK:STATUS:%s", "任务状态缓存前缀"),

    APPOINTMENT_SUB_TASK_COUNT("APPOINTMENT_SUB_TASK_COUNT:%s", "预约任务子任务处理统计 job唯一标识"),
    COMFY_UI_TASK_FAIL("COMFY_UI_TASK_FAIL:%s", "comfyui任务失败 id"),

    /**
     * 菜单
     */
    PROJECT_MENU_TREE("project:menu:tree:%s", "项目菜单树"),

    /**
     * 蒙版
     */
    SEGMENT_URL_CACHE("segment:url:cache:%s", "蒙版地址缓存"),
    SEGMENT_URL_GET("segment:url:get:%s", "蒙版单个地址缓存"),
    SEGMENT_URL_FILE_GET("segment:url:file:get:%s", "蒙版单个地址缓存"),

    /**
     * image 4o 代理
     */
    IMAGE_4O_PROXY("IMAGE_4O_PROXY", "4o代理厂商列表"),

    /**
     * agent
     */
    AGENT_N8N_PROGRESS("agent:n8n_progress:%s", "智能体 流程进度"),

    /**
     * 通知
     */
    TASK_ERROR_NOTICE("task:error:notice:%d", "任务失败通知"),
    TASK_ERROR_NOTICE_DAY("task:error:notice:%d:%s", "任务失败通知日期"),

    /**
     * mind点
     */
    AI_MIND_ACCOUNT_ADD("ai:mind:account:add:%d:%s", "新增mind点账户"),

    /**
     * 锁
     */
    SYNC_QUEUE_LOCK("sync:queue:lock", "同步队列"),
    CHECK_TASK_STATUS_ASYNC_QUEUE_LOCK("check:task:status:async:lock:%s", "检查队列状态"),
    CHECK_COMPANY_END_TIME_LOCK("check:company:end:time:lock", "校验公司到期任务"),
    SCAN_EXPIRING_CONTAINERS_LOCK("scan:expiring:containers:%s", "即将过期的容器"),
    COMFY_UI_URL_CACHE_LOCK("comfyUI:url:cache:lock:%d", "ComfyUI地址缓存锁"),
    COMFY_UI_FILE_ADD_LOCK("comfyUI:file:add:lock:%s", "ComfyUI文件新增锁"),
    COMFY_UI_QUEUE_LOCK("comfyUI:url:queue:lock:%s:", "ComfyUI队列锁"),
    SEGMENT_URL_CACHE_LOCK("segment:url:cache:lock:%s", "智能选区蒙版锁"),
    AI_CREATE_TASK_LOCK("ai:create:task:lock:%s", "创建任务锁"),
    ;

    /**
     * key格式化
     * @param cacheKeyEnums 枚举
     * @param args 参数
     * @return 格式化结果
     */
    public static String format(CacheKeyEnum cacheKeyEnums, Object... args) {
        return String.format(cacheKeyEnums.getKey(), args);
    }

    private final String key;

    private final String value;


    CacheKeyEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }
}

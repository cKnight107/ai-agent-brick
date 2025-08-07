package com.agent.brick.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缓存 key 统一枚举
 * @author chenKai
 * @since 2024/6/11
 * @author chenKai
 */
@AllArgsConstructor
public enum CacheKeyEnum {


    ADMIN_LOGIN_KEY("admin:login:%s:%s", "登录缓存key userId token"),
    ;

    public String format(Object... args){
        return String.format(this.key, args);
    }

    public final String key;
    public final String value;

}

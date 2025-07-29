package com.agent.brick.enums;

import lombok.Getter;

/**
 * 统一业务枚举
 * @author cKnight
 * @since 2024/6/11
 */
@Getter
public enum BizCodeEnum {
    /**
     * 系统级 响应枚举
     */
    SUCCESS(0, "成功"),
    FAIL(-1, "网络异常,请稍后重试"),
    UNIQUE_ERROR(-999, "唯一值重复，请检查"),
    ILLEGAL_REQUEST(10001,"非法请求"),
    SYS_ARGS_ERROR(11001,"参数缺少请检查！"),
    SYS_REQUIRE_ERROR(403,"暂无权限"),
    SYS_USER_LOGIN_EXPIRE(12001,"登录失效,请重新登录"),
    SYS_USER_LOGIN_ERROR(12002,"账号或密码错误,请重新登录"),
    SYS_USER_EXIST_ERROR(13004,"当前账户已存在，不可重复添加"),
    SYS_USER_STATUS_OFF_ERROR(13007, "账户已被锁定，请联系管理员")

    ;


    private Integer code;
    private String msg;

    BizCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

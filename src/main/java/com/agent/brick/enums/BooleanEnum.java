package com.agent.brick.enums;

import lombok.AllArgsConstructor;

/**
 * 是否 相关枚举
 * @author cKnight
 * @since 2024/6/9
 * @author cKnight
 */
@AllArgsConstructor
public enum BooleanEnum {
    YES("是"),
    NO("否"),

    ONE("是"),
    ZERO("否"),

    ON("开启"),
    OFF("关闭"),

    START("开始"),
    END("结束"),

    SUCCESS("成功"),
    FAIL("失败"),
    ;


    public static Boolean convert(String value){
        BooleanEnum booleanEnum = BooleanEnum.valueOf(value);
        switch (booleanEnum){
            case YES, ONE, START, SUCCESS, ON:
                return Boolean.TRUE;
            default:return Boolean.FALSE;
        }
    }

    public final String label;

}

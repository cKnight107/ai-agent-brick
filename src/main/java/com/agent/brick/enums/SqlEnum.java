package com.agent.brick.enums;

import lombok.Getter;

/**
 * sql枚举
 * @author cKnight
 * @since 2024/6/19
 */
@Getter
public enum SqlEnum {
    LIMIT_ONE("最终一个","LIMIT 1"),
    LIMIT("最终","LIMIT "),

    ASC("正序", "ASC"),
    DESC("倒叙", "DESC")
    ;


    private String label;

    private String value;

    SqlEnum(String label, String value) {
        this.label = label;
        this.value = value;
    }
}

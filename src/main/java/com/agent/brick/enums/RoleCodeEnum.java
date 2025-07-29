package com.agent.brick.enums;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/7/4
 */
@AllArgsConstructor
public enum RoleCodeEnum {
    TEACHER("教师"),
    STUDENT("学生"),
    ;

    public static List<RoleCodeEnum> valueList(List<String> names){
        return names.stream().map(RoleCodeEnum::valueOf).collect(Collectors.toList());
    }

    public final String label;
}

package com.agent.brick.pojo.dto;


import com.agent.brick.enums.RoleCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户缓存类
 * @author cKnight
 * @since 2024/6/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysCacheUserDto {
    private Long id;
    /**
     * 账号
     */
    private String accountNo;

    /**
     * 昵称
     */
    private String name;

    /**
     * 用户角色
     */
    private List<RoleCodeEnum> roleList;

    @Override
    public String toString() {
        return STR."userId(用户ID)=\{id}, accountNo(用户账号)=\{accountNo}, name(用户名称)=\{name}, roleList(用户角色列表)=\{roleList.stream().map(obj->STR."\{obj}(\{obj.label})").collect(Collectors.joining("、"))}";
    }
}

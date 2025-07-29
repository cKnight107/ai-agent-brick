package com.agent.brick.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 拦截器 信息实体类
 * @author cKnight
 * @since 2024/6/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterceptorDto {

    /**
     * 令牌
     */
    private String token;

    /**
     * 用户id
     */
    private Long userId;
}

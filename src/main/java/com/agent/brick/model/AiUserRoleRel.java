package com.agent.brick.model;

import com.agent.brick.base.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;


/**
 * <p>
 * 
 * </p>
 *
 * @author autoCode
 * @since 2025-07-04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("ai_user_role_rel")
public class AiUserRoleRel extends BaseDO {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;
}

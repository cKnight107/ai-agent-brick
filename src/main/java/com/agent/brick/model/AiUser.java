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
 * @since 2025-06-15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("ai_user")
public class AiUser extends BaseDO {

    /**
     * 账号
     */
    @TableField("account_no")
    private String accountNo;

    /**
     * 昵称
     */
    @TableField("name")
    private String name;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 加密盐
     */
    @TableField("salt")
    private String salt;

    /**
     * 状态
     */
    @TableField("status")
    private Boolean status;
}

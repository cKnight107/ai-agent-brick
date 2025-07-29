package com.agent.brick.base;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author cKnight
 * @since 2024/7/30
 */
@Data
public class BaseDO {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建时间 数据库层面添加
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 最后更新时间 数据库层面添加
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 创建人
     */
    @TableField(value = "create_by",fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 修改人
     */
    @TableField(value = "update_by",fill = FieldFill.UPDATE)
    private String updateBy;

    /**
     * 逻辑删除 自主添加条件 数据库层面添加
     */
    @TableLogic(value = "false",delval = "true")
    @TableField(value = "del_flag")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean delFlag;
}

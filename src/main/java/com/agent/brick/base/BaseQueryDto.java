package com.agent.brick.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 统一查询基础类 由其他 request类继承
 * @since  2024/6/11
 * @author cKnight
 */
@Data
public class BaseQueryDto {
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private boolean isDel;

    private int pageNum = 1;

    private int pageSize = 10;
}

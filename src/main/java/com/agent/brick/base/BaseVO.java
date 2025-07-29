package com.agent.brick.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/7/5
 */
@Data
public class BaseVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
}

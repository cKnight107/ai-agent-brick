package com.agent.brick.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cKnight
 * @since 2024/6/18
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PageVO<T> {

    private Long totalRecord;

    private Long totalPage;

    private List<T> rows = new ArrayList<>();
}

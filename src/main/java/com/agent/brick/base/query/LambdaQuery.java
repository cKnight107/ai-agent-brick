package com.agent.brick.base.query;

import com.agent.brick.base.BaseDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.baomidou.mybatisplus.core.enums.SqlKeyword.*;

/**
 * 查询基类
 * @author cKnight
 * @since 2024/7/30
 */
public class LambdaQuery<T extends BaseDO> extends LambdaQueryWrapper<T> {
    @Getter
    private final List<SFunction<T, ?>> columns = new ArrayList<>();

    /**
     * 排序map
     */
    private final LinkedHashMap<String, String> orderMap = new LinkedHashMap<>();

    /**
     * 添加排序
     */
    public void addOrder(boolean condition, SFunction<T, ?> column, boolean isAsc) {
        if (condition) {
            String columnName = columnToSqlSegment(column).getSqlSegment();
            orderMap.put(columnName, columnName + StringPool.SPACE + (isAsc ? ASC : DESC).getSqlSegment());
        }
    }

    public void applyConfig() {
        if (CollectionUtils.isNotEmpty(columns)) {
            //默认查出id
            columns.add(T::getId);
            this.select(columns.toArray(new SFunction[0]));
        }
        if (MapUtils.isNotEmpty(orderMap)) {
            appendSqlSegments(
                    ORDER_BY,
                    () -> StringUtils.join(orderMap.values(), StringPool.COMMA)
            );
        }
    }
}

package com.agent.brick.base.mapper;

import com.agent.brick.base.BaseDO;
import com.agent.brick.base.query.LambdaQuery;
import com.agent.brick.base.query.QueryHead;
import com.agent.brick.base.query.QueryTail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 自定义的base mapper
 * @author chenKai
 * @since 2024/7/12
 */
public interface CustomBaseMapper<T extends BaseDO> extends BaseMapper<T> {
    default QueryTail<T> where() {
        return new QueryTail<>(this, new LambdaQuery<>());
    }
    default QueryHead<T> select() {
        return new QueryHead<>(this);
    }
}

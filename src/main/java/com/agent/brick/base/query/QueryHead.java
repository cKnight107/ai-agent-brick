package com.agent.brick.base.query;

import com.agent.brick.base.BaseDO;
import com.agent.brick.base.mapper.CustomBaseMapper;
import com.agent.brick.util.ConvertUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.io.Serializable;
import java.util.List;

/**
 * 查询头
 * @author cKnight
 * @since 2024/7/30
 */
public class QueryHead<T extends BaseDO> {
    private final CustomBaseMapper<T> mapper;
    private final LambdaQuery<T> wrapper = new LambdaQuery<>();

    public QueryHead(CustomBaseMapper<T> mapper){
        this.mapper = mapper;
    }

    /** 查询字段 列表 */
    @SafeVarargs
    public final QueryHead<T> cols(SFunction<T, ?>... functions){
        for (SFunction<T, ?> function : functions) {
            wrapper.getColumns().add(function);
        }
        return this;
    }

    /** 查询单个字段 */
    public QueryHead<T> col(SFunction<T,?> function){
        return col(true,function);
    }

    /** 带条件的单个字段 */
    public QueryHead<T> col(boolean condition,SFunction<T,?> function){
        if (condition){
            wrapper.getColumns().add(function);
        }
        return this;
    }

    /** 升序 */
    public List<T> orderAsc(SFunction<T,?> function){
        return orderAsc(true,function);
    }

    public List<T> orderAsc(boolean condition,SFunction<T,?> function){
        wrapper.applyConfig();
        wrapper.orderByAsc(condition,function);
        return mapper.selectList(wrapper);
    }

    /** 降序 */
    public List<T> orderDesc(SFunction<T,?> function){
        return orderDesc(true,function);
    }

    public List<T> orderDesc(boolean condition , SFunction<T,?> function){
        wrapper.applyConfig();
        wrapper.orderByDesc(condition,function);
        return mapper.selectList(wrapper);
    }

    /** 根据id查询 */
    public T byId(Serializable id){
        wrapper.applyConfig();
        return mapper.selectById(id);
    }

    /** 查询全部 */
    public List<T> list(){
        wrapper.applyConfig();
        return mapper.selectList(wrapper);
    }


    public <E> List<E> list(Class<E> clazz){
        wrapper.applyConfig();
        return ConvertUtils.listFromTo(mapper.selectList(wrapper),clazz);
    }

    public QueryTail<T> where(){
        return new QueryTail<>(mapper,wrapper);
    }

}

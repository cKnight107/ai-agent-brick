package com.agent.brick.base.query;

import com.agent.brick.base.BaseDO;
import com.agent.brick.base.BaseQueryDto;
import com.agent.brick.base.mapper.CustomBaseMapper;
import com.agent.brick.enums.SqlEnum;
import com.agent.brick.pojo.vo.PageVO;
import com.agent.brick.util.ConvertUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 查询尾
 * @author cKnight
 * @since  2024/7/30
 */
public class QueryTail<T extends BaseDO> {
    private final CustomBaseMapper<T> mapper;
    private final LambdaQuery<T> wrapper;

    public QueryTail(CustomBaseMapper<T> mapper, LambdaQuery<T> wrapper) {
        this.mapper = mapper;
        this.wrapper = wrapper;
    }

    /**
     * 查询字段 列表
     */
    @SafeVarargs
    public final QueryTail<T> cols(SFunction<T, ?>... functions) {
        for (SFunction<T, ?> function : functions) {
            wrapper.getColumns().add(function);
        }
        return this;
    }

    public T one() {
        wrapper.applyConfig();
        return mapper.selectOne(wrapper);
    }

    public <O> O one(Class<O> clazz) {
        wrapper.applyConfig();
        return ConvertUtils.beanProcess(mapper.selectOne(wrapper), clazz);
    }

    /**
     * 正常list查询
     */
    public List<T> list() {
        wrapper.applyConfig();
        return mapper.selectList(wrapper);
    }

    public <R> List<R> list(Function<T, R> function) {
        wrapper.applyConfig();
        return ConvertUtils.toFieldList(mapper.selectList(wrapper), function);
    }

    public <E> List<E> list(Class<E> clazz) {
        wrapper.applyConfig();
        return ConvertUtils.listFromTo(mapper.selectList(wrapper), clazz);
    }


    /**
     * 带有fun的去重
     */
    public <R> List<R> distinct(Function<T, R> function) {
        return list().stream().map(function).distinct().collect(Collectors.toList());
    }

    /**
     * 不带fun的去重
     */
    public List<T> distinct() {
        return list().stream().distinct().collect(Collectors.toList());
    }

    public PageVO<T> page(long pageNum, long pageSize) {
        wrapper.applyConfig();
        Page<T> page = mapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return ConvertUtils.toPageResult(page);
    }

    /**
     * 正常分页查询
     */
    public PageVO<T> page(BaseQueryDto query) {
        wrapper.applyConfig();
        Page<T> page = mapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return ConvertUtils.toPageResult(page);
    }

    public <E> PageVO<E> page(BaseQueryDto query, Class<E> clazz) {
        wrapper.applyConfig();
        Page<T> page = mapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return ConvertUtils.toPageResult(page, clazz);
    }


    public T limitOne() {
        wrapper.applyConfig();
        wrapper.last(SqlEnum.LIMIT_ONE.getValue());
        return mapper.selectOne(wrapper);
    }

    public List<T> limit(int num) {
        wrapper.applyConfig();
        wrapper.last(SqlEnum.LIMIT.getValue() + num);
        return mapper.selectList(wrapper);
    }

    public List<T> limit(int num1, int num2) {
        wrapper.applyConfig();
        wrapper.last(SqlEnum.LIMIT.getValue() + num1 + ", " + num2);
        return mapper.selectList(wrapper);
    }

    public Long count() {
        return mapper.selectCount(wrapper);
    }

    public QueryTail<T> orderAsc(SFunction<T, ?> function) {
        wrapper.addOrder(true, function, true);
        return this;
    }


    public QueryTail<T> orderAsc(boolean condition, SFunction<T, ?> function) {
        wrapper.addOrder(condition, function, true);
        return this;
    }

    public QueryTail<T> orderDesc(boolean condition, SFunction<T, ?> function) {
        wrapper.addOrder(condition, function, false);
        return this;
    }

    public QueryTail<T> orderDesc(SFunction<T, ?> function) {
        wrapper.addOrder(true, function, false);
        return this;
    }

    /**
     * 返回指定 K ，V 是 T(DO类)
     */
    public <R> Map<R, List<T>> hasMap(Function<T, R> keyFun) {
        return list().stream().collect(Collectors.groupingBy(keyFun));
    }

    /**
     * 返回指定 K V
     */
    public <K, V> Map<K, V> hasMap(Function<T, K> keyFun, Function<T, V> valFun) {
        return list().stream().collect(Collectors.toMap(keyFun, valFun));
    }

    /**
     * 返回指定 K V
     */
    public <K, V> Map<K, V> hasMap(Function<T, K> keyFun, Function<T, V> valFun, BinaryOperator<V> mergeFunction) {
        return list().stream().collect(Collectors.toMap(keyFun, valFun, mergeFunction));
    }


    /**
     * 返回hash set
     */
    public <K> Set<K> hasSet(Function<T, K> keyFun) {
        return list().stream().map(keyFun).collect(Collectors.toSet());
    }

    /**
     * id索引优化
     */
    public QueryTail<T> idIndex() {
        wrapper.apply("id >= {0}", 0);
        return this;
    }

    public QueryTail<T> delFlag() {
        wrapper.apply("del_flag = {0}", 1);
        return this;
    }

    public QueryTail<T> delFlag(boolean condition) {
        if (condition) {
            wrapper.apply("del_flag = {0}", 1);
        }
        return this;
    }

    public QueryTail<T> betweenTime(BaseQueryDto queryDto, SFunction<T, ?> column) {
        wrapper.between(Objects.nonNull(queryDto.getStartTime()) && Objects.nonNull(queryDto.getEndTime()),
                column,
                queryDto.getStartTime(), queryDto.getEndTime());
        return this;
    }

    public QueryTail<T> betweenTime(BaseQueryDto queryDto) {
        return betweenTime(queryDto, T::getCreateTime);
    }


    public T byId(Long id) {
        wrapper.applyConfig();
        wrapper.eq(T::getId, id);
        return mapper.selectOne(wrapper);
    }


    //=========================mp 提供能力=========================

    public QueryTail<T> eq(SFunction<T, ?> column, Object val) {
        wrapper.eq(column, val);
        return this;
    }

    public QueryTail<T> eq(boolean condition, SFunction<T, ?> column, Object val) {
        wrapper.eq(condition, column, val);
        return this;
    }

    public QueryTail<T> ne(SFunction<T, ?> column, Object val) {
        wrapper.ne(column, val);
        return this;
    }

    public QueryTail<T> ne(boolean condition, SFunction<T, ?> column, Object val) {
        wrapper.ne(condition, column, val);
        return this;
    }

    public QueryTail<T> gt(SFunction<T, ?> column, Object val) {
        wrapper.gt(column, val);
        return this;
    }

    public QueryTail<T> gt(boolean condition, SFunction<T, ?> column, Object val) {
        wrapper.gt(condition, column, val);
        return this;
    }

    public QueryTail<T> ge(SFunction<T, ?> column, Object val) {
        wrapper.ge(column, val);
        return this;
    }

    public QueryTail<T> ge(boolean condition, SFunction<T, ?> column, Object val) {
        wrapper.ge(condition, column, val);
        return this;
    }

    public QueryTail<T> lt(SFunction<T, ?> column, Object val) {
        wrapper.lt(column, val);
        return this;
    }

    public QueryTail<T> lt(boolean condition, SFunction<T, ?> column, Object val) {
        wrapper.lt(condition, column, val);
        return this;
    }

    public QueryTail<T> le(SFunction<T, ?> column, Object val) {
        wrapper.le(column, val);
        return this;
    }

    public QueryTail<T> le(boolean condition, SFunction<T, ?> column, Object val) {
        wrapper.le(condition, column, val);
        return this;
    }

    public QueryTail<T> like(SFunction<T, ?> column, Object val) {
        wrapper.like(column, val);
        return this;
    }

    public QueryTail<T> like(boolean condition, SFunction<T, ?> column, Object val) {
        wrapper.like(condition, column, val);
        return this;
    }

    public QueryTail<T> notLike(SFunction<T, ?> column, Object val) {
        wrapper.notLike(column, val);
        return this;
    }

    public QueryTail<T> notLike(boolean condition, SFunction<T, ?> column, Object val) {
        wrapper.notLike(condition, column, val);
        return this;
    }

    public QueryTail<T> likeLeft(SFunction<T, ?> column, Object val) {
        wrapper.likeLeft(column, val);
        return this;
    }

    public QueryTail<T> likeLeft(boolean condition, SFunction<T, ?> column, Object val) {
        wrapper.likeLeft(condition, column, val);
        return this;
    }

    public QueryTail<T> likeRight(SFunction<T, ?> column, Object val) {
        wrapper.likeRight(column, val);
        return this;
    }

    public QueryTail<T> likeRight(boolean condition, SFunction<T, ?> column, Object val) {
        wrapper.likeRight(condition, column, val);
        return this;
    }

    public QueryTail<T> between(SFunction<T, ?> column, Object val1, Object val2) {
        wrapper.between(column, val1, val2);
        return this;
    }

    public QueryTail<T> between(boolean condition, SFunction<T, ?> column, Object val1, Object val2) {
        wrapper.between(condition, column, val1, val2);
        return this;
    }

    public QueryTail<T> notBetween(SFunction<T, ?> column, Object val1, Object val2) {
        wrapper.notBetween(column, val1, val2);
        return this;
    }

    public QueryTail<T> notBetween(boolean condition, SFunction<T, ?> column, Object val1, Object val2) {
        wrapper.notBetween(condition, column, val1, val2);
        return this;
    }

    public QueryTail<T> and(Consumer<LambdaQueryWrapper<T>> consumer) {
        wrapper.and(consumer);
        return this;
    }

    public QueryTail<T> and(boolean condition, Consumer<LambdaQueryWrapper<T>> consumer) {
        wrapper.and(condition, consumer);
        return this;
    }

    public QueryTail<T> or(Consumer<LambdaQueryWrapper<T>> consumer) {
        wrapper.or(consumer);
        return this;
    }

    public QueryTail<T> or(boolean condition, Consumer<LambdaQueryWrapper<T>> consumer) {
        wrapper.or(condition, consumer);
        return this;
    }

    public QueryTail<T> nested(Consumer<LambdaQueryWrapper<T>> consumer) {
        wrapper.nested(consumer);
        return this;
    }

    public QueryTail<T> nested(boolean condition, Consumer<LambdaQueryWrapper<T>> consumer) {
        wrapper.nested(condition, consumer);
        return this;
    }

    public QueryTail<T> not(Consumer<LambdaQueryWrapper<T>> consumer) {
        wrapper.not(consumer);
        return this;
    }

    public QueryTail<T> not(boolean condition, Consumer<LambdaQueryWrapper<T>> consumer) {
        wrapper.not(condition, consumer);
        return this;
    }

    public QueryTail<T> or(boolean condition) {
        wrapper.or(condition);
        return this;
    }

    public QueryTail<T> apply(String applySql, Object... value) {
        wrapper.apply(applySql, value);
        return this;
    }

    public QueryTail<T> apply(boolean condition, String applySql, Object... value) {
        wrapper.apply(condition, applySql, value);
        return this;
    }

    public QueryTail<T> last(String lastSql) {
        wrapper.last(lastSql);
        return this;
    }

    public QueryTail<T> last(boolean condition, String lastSql) {
        wrapper.last(condition, lastSql);
        return this;
    }

    public QueryTail<T> comment(String comment) {
        wrapper.comment(comment);
        return this;
    }

    public QueryTail<T> comment(boolean condition, String comment) {
        wrapper.comment(condition, comment);
        return this;
    }

    public QueryTail<T> first(String firstSql) {
        wrapper.first(firstSql);
        return this;
    }

    public QueryTail<T> first(boolean condition, String firstSql) {
        wrapper.first(condition, firstSql);
        return this;
    }

    public QueryTail<T> exists(String existsSql) {
        wrapper.exists(existsSql);
        return this;
    }

    public QueryTail<T> exists(boolean condition, String existsSql) {
        wrapper.exists(condition, existsSql);
        return this;
    }

    public QueryTail<T> notExists(String existsSql) {
        wrapper.notExists(existsSql);
        return this;
    }

    public QueryTail<T> notExists(boolean condition, String existsSql) {
        wrapper.notExists(condition, existsSql);
        return this;
    }

    public QueryTail<T> isNull(SFunction<T, ?> column) {
        wrapper.isNull(column);
        return this;
    }

    public QueryTail<T> isNull(boolean condition, SFunction<T, ?> column) {
        wrapper.isNull(condition, column);
        return this;
    }

    public QueryTail<T> isNotNull(SFunction<T, ?> column) {
        wrapper.isNotNull(column);
        return this;
    }

    public QueryTail<T> isNotNull(boolean condition, SFunction<T, ?> column) {
        wrapper.isNotNull(condition, column);
        return this;
    }

    public QueryTail<T> in(SFunction<T, ?> column, Collection<?> coll) {
        wrapper.in(column, coll);
        return this;
    }

    public QueryTail<T> in(boolean condition, SFunction<T, ?> column, Collection<?> coll) {
        wrapper.in(condition, column, coll);
        return this;
    }

    public QueryTail<T> notIn(SFunction<T, ?> column, Collection<?> coll) {
        wrapper.notIn(column, coll);
        return this;
    }

    public QueryTail<T> notIn(boolean condition, SFunction<T, ?> column, Collection<?> coll) {
        wrapper.notIn(condition, column, coll);
        return this;
    }

    public QueryTail<T> inSql(SFunction<T, ?> column, String inValue) {
        wrapper.inSql(column, inValue);
        return this;
    }

    public QueryTail<T> inSql(boolean condition, SFunction<T, ?> column, String inValue) {
        wrapper.inSql(condition, column, inValue);
        return this;
    }

    public QueryTail<T> notInSql(SFunction<T, ?> column, String inValue) {
        wrapper.notInSql(column, inValue);
        return this;
    }

    public QueryTail<T> notInSql(boolean condition, SFunction<T, ?> column, String inValue) {
        wrapper.notInSql(condition, column, inValue);
        return this;
    }

    public QueryTail<T> groupBy(SFunction<T, ?> column) {
        wrapper.groupBy(true, column);
        return this;
    }


    public QueryTail<T> groupBy(boolean condition, SFunction<T, ?> column) {
        wrapper.groupBy(condition, column);
        return this;
    }


    public QueryTail<T> groupBy(boolean condition, List<SFunction<T, ?>> columns) {
        wrapper.groupBy(condition, columns);
        return this;
    }


    public QueryTail<T> having(String sqlHaving, Object... params) {
        wrapper.having(sqlHaving, params);
        return this;
    }

    public QueryTail<T> having(boolean condition, String sqlHaving, Object... params) {
        wrapper.having(condition, sqlHaving, params);
        return this;
    }

    public QueryTail<T> func(Consumer<LambdaQueryWrapper<T>> consumer) {
        wrapper.func(consumer);
        return this;
    }

    public QueryTail<T> func(boolean condition, Consumer<LambdaQueryWrapper<T>> consumer) {
        wrapper.func(condition, consumer);
        return this;
    }

}

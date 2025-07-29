package com.agent.brick.base;

import com.agent.brick.base.mapper.CustomBaseMapper;
import com.agent.brick.base.query.QueryHead;
import com.agent.brick.base.query.QueryTail;
import com.agent.brick.compant.AuthComponent;
import com.agent.brick.compant.RedisCacheComponent;
import com.agent.brick.enums.BizCodeEnum;
import com.agent.brick.exception.BizException;
import com.agent.brick.interceptor.LoginInterceptor;
import com.agent.brick.pojo.dto.InterceptorDto;
import com.agent.brick.pojo.dto.SysCacheUserDto;
import com.agent.brick.pojo.vo.JsonResult;
import com.agent.brick.util.SpringContextUtils;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基础服务类 其他微服务中的serviceImpl 继承此类 快速获取相关信息
 * @since  2024/6/11
 * @author cKnight
 */
@Component
@Slf4j
public class BaseService {
    @Resource
    private AuthComponent authComponent;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    private RedisCacheComponent redisCacheComponent;

    /**
     * 获取用户信息
     */
    public SysCacheUserDto getAdminUserInfo() {
        return authComponent.getAdminUserInfo();
    }

    public InterceptorDto getInterceptorDto() {
        InterceptorDto interceptorDto = LoginInterceptor.loginThreadLocal.get();
        if (Objects.nonNull(interceptorDto)) {
            return interceptorDto;
        }
        return null;
    }


    public <T extends CustomBaseMapper<E>, E extends BaseDO> QueryHead<E> select(Class<T> clazz) {
        return select(Objects.requireNonNull(SpringContextUtils.getBean(clazz),STR."baseService select 未获取bean,class:\{clazz.getSimpleName()}"));
    }


    /**
     * 快速批量插入
     * @param list 实体列表
     * @param clazz mapper class
     * @param <T> mapper
     * @param <E> do
     */
    public <T extends CustomBaseMapper<E> ,E extends BaseDO> void insertBatch(List<E> list,Class<T> clazz){
        MybatisBatch<E> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,list);
        MybatisBatch.Method<E> method = new MybatisBatch.Method<>(clazz);
        mybatisBatch.execute(method.insert());
    }

    public <T extends CustomBaseMapper<E>, E extends BaseDO> QueryTail<E> where(Class<T> clazz) {
        return where(Objects.requireNonNull(SpringContextUtils.getBean(clazz), STR."baseService where 未获取bean,class:\{clazz.getSimpleName()}"));
    }

    public <E extends BaseDO> void checkUnique(QueryTail<E> where){
        checkUnique(where,BizCodeEnum.UNIQUE_ERROR);
    }

    /**
     * 快速获取where 条件
     */
    public <T extends CustomBaseMapper<E>, E extends BaseDO> QueryTail<E> where(T t) {
        return t.where();
    }


    /**
     * 快速 唯一条件校验
     * @param where 查询条件
     * @param bizCodeEnum 抛出异常
     * @param <E> 实体类
     */
    public <E extends BaseDO> void checkUnique(QueryTail<E> where, BizCodeEnum bizCodeEnum) {
        Long count = where.count();
        if (count > 0) {
            throw new BizException(bizCodeEnum);
        }
    }


    /**
     * 根据条件查询是否存在
     * @param where 条件
     * @param <E> 实体类
     * @return boolean
     */
    public < E extends BaseDO> boolean isExist(QueryTail<E> where) {
        return where.count() > 0;
    }


    /**
     * 快速获取查询语句
     * @param t mapper
     * @param <T> mapper
     * @param <E> do
     * @return select
     */
    public <T extends CustomBaseMapper<E>, E extends BaseDO> QueryHead<E> select(T t) {
        return t.select();
    }

    /**
     * 开始时间与结束时间都不为空
     */
    public boolean queryTimeIsNotNull(BaseQueryDto baseQueryDto) {
        return Objects.nonNull(baseQueryDto.getStartTime()) && Objects.nonNull(baseQueryDto.getEndTime());
    }

    public <T> void checkFeign(JsonResult<T> result) {
        if (!BizCodeEnum.SUCCESS.getCode().equals(result.getCode())) {
            throw new BizException(result.getCode(), result.getMsg());
        }
    }

    public <R, T> Map<R, List<T>> hasMap(List<T> list, Function<T, R> keyFun) {
        return list.stream().collect(Collectors.groupingBy(keyFun));
    }

    public <K, V, T> Map<K, V> hasMap(List<T> list, Function<T, K> keyFun, Function<T, V> valFun) {
        return list.stream().collect(Collectors.toMap(keyFun, valFun));
    }

    public <K, V, T> Map<K, List<V>> hasMapListV(List<T> list, Function<T, K> keyFun, Function<T, V> valFun) {
        return list.stream().collect(Collectors.groupingBy(keyFun, Collectors.mapping(valFun, Collectors.toList())));
    }

}

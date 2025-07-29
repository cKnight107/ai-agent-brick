package com.agent.brick.util;

import com.agent.brick.enums.BizCodeEnum;
import com.agent.brick.exception.BizException;
import com.agent.brick.pojo.vo.PageVO;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 转换相关工具类
 * @author cKnight
 * @since 2024/6/11
 *
 * @author cKnight
 */
public class ConvertUtils {


    /**
     * pageVO转化 需要 DO 转 VO
     *
     * @param page    分页
     * @param toClass 转化的类
     * @param <I>     源对象类型
     * @param <O>     转换对象类型
     * @return 分页VO
     */
    public static <I, O> PageVO<O> toPageResult(Page<I> page, Class<O> toClass) {
        PageVO<O> result = new PageVO<>();
        result.setTotalPage(page.getPages());
        result.setTotalRecord(page.getTotal());
        result.setRows(listFromTo(page.getRecords(), toClass));
        return result;
    }

    /**
     * pageVO转化
     *
     * @param page 分页
     * @param <I>  入参类型
     * @return 分页VO
     */
    public static <I> PageVO<I> toPageResult(Page<I> page) {
        PageVO<I> result = new PageVO<>();
        result.setTotalPage(page.getPages());
        result.setTotalRecord(page.getTotal());
        result.setRows(page.getRecords());
        return result;
    }

    /**
     * pageVO转化 需要 DO 转 VO
     *
     * @param page 分页
     * @param list 转化的集合
     * @param <I>  源对象类型
     * @param <O>  转换对象类型
     * @return 分页VO
     */
    public static <I, O> PageVO<O> toPageResult(Page<I> page, List<O> list) {
        PageVO<O> result = new PageVO<>();
        result.setTotalPage(page.getPages());
        result.setTotalRecord(page.getTotal());
        result.setRows(list);
        return result;
    }

    /**
     * pageVO转化
     *
     * @param page 分页VO
     * @param list 转化的集合
     * @param <I>  源对象类型
     * @param <O>  转换对象类型
     * @return 分页VO
     */
    public static <I, O> PageVO<O> toPageResult(PageVO<I> page, List<O> list) {
        PageVO<O> result = new PageVO<>();
        result.setTotalPage(page.getTotalPage());
        result.setTotalRecord(page.getTotalRecord());
        result.setRows(list);
        return result;
    }

    /**
     * pageVO转化
     *
     * @param page  分页VO
     * @param clazz 转化的集合
     * @param <I>   源对象类型
     * @param <O>   转换对象类型
     * @return 分页VO
     */
    public static <I, O> PageVO<O> toPageResult(PageVO<I> page, Class<O> clazz) {
        PageVO<O> result = new PageVO<>();
        result.setTotalPage(page.getTotalPage());
        result.setTotalRecord(page.getTotalRecord());
        result.setRows(listFromTo(page.getRows(), clazz));
        return result;
    }

    /**
     * pageVO转化
     *
     * @param total 总数
     * @param pages 总页数
     * @param list  转化的集合
     * @param <O>   出参类型
     * @return 分页VO
     */
    public static <O> PageVO<O> toPageResult(Long total, Long pages, List<O> list) {
        PageVO<O> result = new PageVO<>();
        result.setTotalPage(pages);
        result.setTotalRecord(total);
        result.setRows(list);
        return result;
    }

    /**
     * 单个对象转换
     *
     * @param from  源对象
     * @param clazz 目标对象类
     * @param <I>   源对象类型
     * @param <O>   转换对象类型
     * @return 目标对象
     */
    public static <I, O> O beanProcess(I from, Class<O> clazz) {
        if (Objects.isNull(from)) {
            throw new BizException(BizCodeEnum.FAIL);
        }
        try {
            O o = clazz.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(from, o);
            return o;
        } catch (Exception e) {
            throw new BizException(BizCodeEnum.FAIL);
        }
    }


    /**
     * list DO 转 VO
     *
     * @param list  源集合
     * @param clazz 目标集合对象类
     * @param <I>   源对象类型
     * @param <O>   转换对象类型
     * @return 目标对象集合
     */
    public static <I, O> List<O> listFromTo(List<I> list, Class<O> clazz) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(obj -> beanProcess(obj, clazz))
                .collect(Collectors.toList());
    }


    /**
     * list转字段list
     *
     * @param list     源集合
     * @param function 源集合对象字段
     * @param <I>      源对象类型
     * @param <O>      转换对象类型
     * @return 源集合对象字段集合
     */
    public static <I, O> List<O> toFieldList(List<I> list, Function<I, O> function) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(function).collect(Collectors.toList());
    }
}

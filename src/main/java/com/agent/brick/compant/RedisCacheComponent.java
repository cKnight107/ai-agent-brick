package com.agent.brick.compant;

import com.agent.brick.constants.TimeConstant;
import com.agent.brick.enums.BizCodeEnum;
import com.agent.brick.enums.CacheKeyEnum;
import com.agent.brick.exception.BizException;
import com.agent.brick.process.Process;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
/**
 * redis 通用组件
 * @author cKnight
 * @since 2024/6/11
 */
@Component
@Slf4j
public class RedisCacheComponent {
    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(毫秒)
     * @return
     */
    public boolean expire(String key, long time) {
        if (time > 0) {
            redisTemplate.expire(key, time, TimeUnit.MILLISECONDS);
        }
        return true;
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    public long increment(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return Optional.ofNullable(redisTemplate.opsForValue().increment(key, delta)).orElse(0L);
    }

    /**
     * 递减
     * @param key
     * @param num
     * @return
     */
    public Long decrement(String key, long num) {
        return redisTemplate.opsForValue().decrement(key, num);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key
     * @param value
     * @param timeout  存储时间长度
     * @param timeUnit 存储时间单位
     * @return
     */
    public void set(Object key, Object value, Long timeout, TimeUnit timeUnit) {
        if (timeout != null && timeUnit != null) {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key
     * @param value
     * @param timeout 存储时间长度
     * @return
     */
    public void set(Object key, Object value, Long timeout) {
        if (timeout != null) {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 键
     * @param value 值
     */
    public void set(Object key, Object value) {
        set(key, value, null);
    }

    public Object rightPopAndLeftPush(String key) {
        return redisTemplate.opsForList().rightPopAndLeftPush(key, key);
    }


    /**
     * 获得缓存的基本对象
     *
     * @param key
     * @return
     */
    public String get(Object key) {
        Object o = redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(o)) {
            return o.toString();
        }
        return null;
    }

    /**
     * 获得缓存的基本对象
     *
     * @param key
     * @return
     */
    public Object getObject(Object key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除基本对象缓存
     *
     * @param key
     */
    public boolean remove(Object key) {
        return redisTemplate.delete(key);
    }

    public void remove(Object key, Process process) {
        remove(key);
        process.execute();
        remove(key);
    }

    /**
     * 模糊删除
     *
     * @param key
     */
    public void indistinctDelete(Object key) {
        redisTemplate.delete(redisTemplate.keys("*" + key + "*"));
    }


    /**
     * 模糊删除
     *
     * @param key
     */
    public void removeLike(Object key) {
        Set<Object> keys = redisTemplate.keys("*" + key + "*");
        if (keys.size() > 0) {
            for (Object redisKey : keys) {
                redisTemplate.delete(redisKey);
            }
        }
    }

    public Set<Object> rightLike(String key) {
        return redisTemplate.keys(key + "*");
    }

    public void removeLike(String key) {
        Set<Object> keys = redisTemplate.keys(key);
        if (keys.size() > 0) {
            for (Object redisKey : keys) {
                redisTemplate.delete(redisKey);
            }
        }
    }

    /**
     * 模糊查询
     *
     * @param key
     * @return
     */
    public Map<Object, String> getLike(Object key) {
        Set<Object> keys = redisTemplate.keys("*" + key + "*");
        Map<Object, String> map = new HashMap<>(keys.size());
        if (!keys.isEmpty()) {
            for (Object redisKey : keys) {
                map.put(redisKey, get(redisKey));
            }
            return map;
        } else {
            return null;
        }
    }


    /**
     * 模糊删除key
     *
     * @param pattern
     */
    public void deleteCacheWithPattern(String pattern) {
        Set<Object> keys = redisTemplate.keys("*" + pattern + "*");
        redisTemplate.delete(keys);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key
     * @param value
     * @return
     */
    public void setNoExpire(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 对应key是否存在缓存
     *
     * @param key
     * @return
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 缓存失效剩余时间
     *
     * @param key
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 缓存失效剩余时间
     *
     * @param key
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 获得set
     *
     * @param key
     * @return
     */
    public Set<String> getSet(String key) {
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (!CollectionUtils.isEmpty(members)) {
            return members.stream().map(String::valueOf).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }


    /**
     * 获取list的索引
     *
     * @param key   键
     * @param value 值
     */
    public Long getListIndexOf(String key, Object value) {
        return redisTemplate.opsForList().indexOf(key, value);
    }


    /**
     * set list
     * 指定位置set
     *
     * @param key
     * @param index
     * @param value
     */
    public void setListIndex(String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 头插法
     *
     * @param key
     * @param value
     */
    public void lPush(String key, Object... value) {
        redisTemplate.opsForList().leftPushAll(key, value);
    }

    public void lPush(String key, Collection<?> values) {
        for (Object value : values) {
            redisTemplate.opsForList().leftPushAll(key, value);
        }
    }

    /**
     * 尾插法
     *
     * @param key
     * @param value
     */
    public void rPush(String key, Object... value) {
        redisTemplate.opsForList().rightPushAll(key, value);
    }

    /**
     * 头弹出值
     *
     * @param key
     * @return
     */
    public <T> T lPop(String key, Class<T> clazz) {
        return (T) redisTemplate.opsForList().leftPop(key);
    }

    public Object lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 尾弹出值
     *
     * @param key
     * @return
     */
    public <T> T rPop(String key, Class<T> clazz) {
        return (T) redisTemplate.opsForList().rightPop(key);
    }

    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }


    public <T> T rightPopAndLeftPush(String key, Class<T> clazz) {
        return (T) redisTemplate.opsForList().rightPopAndLeftPush(key, key);
    }

    /**
     * 获取指定范围 list
     *
     * @param key
     * @param start
     * @param end
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> getList(String key, Long start, Long end, Class<T> clazz) {
        return listRange(key, start, end)
                .stream()
                .map(obj -> JSONObject.parseObject(JSONObject.toJSONString(obj), clazz))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定范围 list
     *
     * @param key   键
     * @param clazz 类型
     * @param <T>   类型
     * @return list
     */
    public <T> List<T> list(String key, Class<T> clazz) {
        List<Object> objects = listRange(key, 0L, listSize(key));
        return objects.stream().filter(Objects::nonNull)
                .map(obj -> JSONObject.parseObject(JSONObject.toJSONString(obj)).toJavaObject(clazz))
                .collect(Collectors.toList());
    }


    /**
     * 获取全部list
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> getList(String key, Class<T> clazz) {
        return getList(key, 0L, listSize(key), clazz);
    }


    public List<Object> listRange(String key, Long start, Long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 列表size
     *
     * @param key
     * @return
     */
    public Long listSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 分布式锁 lua脚本编写 单一锁
     *
     * @param key
     * @param time 以秒为单位
     * @return 1 拿锁成功  0拿锁失败
     */
    public Long getLockByLua(String key, Long time) {
        //key1是key，ARGV[1]是time
//        String script = "" +
//                "if redis.call('EXISTS',KEYS[1])==0 then " +
//                "redis.call('expire',KEYS[1],ARGV[1]); " +
//                "return 1;" +
//                " else " +
//                "return 0; " +
//                "end;";
        String script = "if redis.call('EXISTS',KEYS[1])==0 then redis.call('set',KEYS[1],ARGV[1]); redis.call('expire',KEYS[1],ARGV[2]); return 1;" +
                " else return 0; end;";

        Long result = redisTemplate.execute(new
                DefaultRedisScript<>(script, Long.class), Arrays.asList(key), 1, time);
        return result;
    }

    /**
     * 可重入锁 同一个value可以同时拿锁
     *
     * @param key
     * @param value
     * @param time  以秒为单位
     * @return 大于0 拿锁成功 等于0 拿锁失败
     */
    public Long getReentrantLockByLua(String key, String value, Long time) {
        //key1是key，ARGV[1]是value,ARGV[2]是time
//        String script = "" +
//                "if redis.call('EXISTS',KEYS[1])==0 " +  //如果key不存在
//                "then redis.call('set',KEYS[1],ARGV[1]); " +
//                "redis.call('expire',KEYS[1],ARGV[2]); " +
//                "return 1;" +
//                " elseif redis.call('get',KEYS[1]) == ARGV[1] " + //key存在 value一致
//                "then return 2;" +
//                " else " +
//                "return 0; " + //否则 返回
//                "end;";

        String script = "if redis.call('EXISTS',KEYS[1])==0 then redis.call('set',KEYS[1],ARGV[1]); redis.call('expire',KEYS[1],ARGV[2]); return 1;" +
                " elseif redis.call('get',KEYS[1]) == ARGV[1] then return 2;" +
                " else return 0; end;";

        Long result = redisTemplate.execute(new
                DefaultRedisScript<>(script, Long.class), Arrays.asList(key), value, time);
        return result;
    }

    /**
     * 分布式锁流程
     *
     * @param key     锁 key
     * @param time    时间 秒为单位
     * @param process 表达式
     */
    public void tryLock(String key, Long time, Process process) {
        //获取分布式锁
        Long lock = getLockByLua(key, time);
        if (lock > 0) {
            try {
                log.info("=========={}正在执行==========", key);
                process.execute();
            } catch (Exception e) {
                log.error("=========={}执行异常==========", key);
                log.error("执行异常", e);
            } finally {
                //解锁
                remove(key);
            }
        } else {
            log.info("=========={}获取分布式锁失败==========", key);
        }
    }

    /**
     * 分布式锁流程
     *
     * @param cacheKeyEnum 锁
     * @param key       参数
     * @param callable   方法
     * @param time      锁时长 秒为单位
     */
    public <T> T tryLock(CacheKeyEnum cacheKeyEnum, String key, Callable<T> callable, Long time) {
        // 获取分布式锁
        String lockKey = cacheKeyEnum.format(key);
        return this.tryLock(lockKey, callable, time);
    }

    /**
     * 分布式锁流程
     *
     * @param cacheKeyEnum 锁
     * @param callable   方法
     * @param time      锁时长 秒为单位
     * @param keys      锁参数
     */
    public <T> T tryLock(CacheKeyEnum cacheKeyEnum, Callable<T> callable, Long time, Object... keys) {
        // 获取分布式锁
        String lockKey = cacheKeyEnum.format(keys);
        return this.tryLock(lockKey, callable, time);
    }

    /**
     * 分布式锁流程
     *
     * @param cacheKeyEnum 锁
     * @param key       参数
     * @param callable   方法
     * @param time      锁时长 秒为单位
     */
    public <T> T tryLock(CacheKeyEnum cacheKeyEnum, Long key, Callable<T> callable, Long time) {
        // 获取分布式锁
        String lockKey = cacheKeyEnum.format(key);
        return this.tryLock(lockKey, callable, time);
    }

    /**
     * 分布式锁流程
     *
     * @param lockKey    锁键
     * @param callable   方法
     * @param time      锁时长 秒为单位
     */
    public <T> T tryLock(String lockKey, Callable<T> callable, Long time) {
        long enterTimeMillis = System.currentTimeMillis();
        // 设置两倍于锁的超时时间
        while (enterTimeMillis + time * 2 * 1000L > System.currentTimeMillis()) {
            Long lock = getLockByLua(lockKey, time);
            if (lock > 0) {
                try {
                    //执行方法
                    return callable.call();
                } catch (BizException e) {
                    throw new BizException(e);
                } catch (Exception e) {
                    log.error("分布式锁方法失败:", e);
                    break;
                } finally {
                    //解锁
                    remove(lockKey);
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        throw new BizException(BizCodeEnum.FAIL);
    }

    /**
     * 分布式锁流程
     *
     * @param cacheKeyEnum 锁
     * @param process   方法
     */
    public void tryLock(CacheKeyEnum cacheKeyEnum, Process process) {
        // 默认锁十分钟
        long time = 60 * 10L;
        this.tryLock(cacheKeyEnum, process, time);
    }

    /**
     * 分布式锁流程
     *
     * @param cacheKeyEnum 锁
     * @param process   方法
     * @param time      锁时长 秒为单位
     */
    public void tryLock(CacheKeyEnum cacheKeyEnum, Process process, Long time) {
        String key = cacheKeyEnum.key;
        this.tryLock(key, time, process);
    }

    /**
     * 分布式锁流程
     *
     * @param cacheKeyEnum 锁
     * @param key 参数
     * @param process   方法
     * @param time      锁时长 秒为单位
     */
    public void tryLock(CacheKeyEnum cacheKeyEnum, String key, Process process, Long time) {
        String lockKey = cacheKeyEnum.format(key);
        this.tryLock(lockKey, time, process);
    }

    /**
     * 默认锁十分钟
     */
    public void tryLock(String key, Process process) {
        tryLock(key, 60 * 10L, process);
    }


    /**
     * 全局统一数据层缓存
     *
     * @param key
     * @param t
     * @param <T>
     * @return
     */
    public <T> T globalCache(String key, T t, Class<T> clazz) {
        //获取缓存
        String cache = get(key);
        if (StringUtils.isNotBlank(cache)) {
            return resultCache(cache, clazz);
        }
        return resultobject(key, t, TimeConstant.ONE_HOUR * 3);
    }


    /**
     * 传入表达式
     *
     * @param key
     * @param clazz
     * @param objectFactory
     * @param <T>
     * @return
     */
    public <T> T globalCache(String key, Class<T> clazz, ObjectFactory<T> objectFactory) {
        //获取缓存
        String cache = get(key);
        if (StringUtils.isNotBlank(cache)) {
            return resultCache(cache, clazz);
        }
        return resultobject(key, objectFactory.getObject(), TimeConstant.ONE_HOUR * 3);
    }


    public <T> T globalCache(String key, Class<T> clazz, ObjectFactory<T> objectFactory, Long time) {
        //获取缓存
        String cache = get(key);
        if (StringUtils.isNotBlank(cache)) {
            return resultCache(cache, clazz);
        }
        return resultobject(key, objectFactory.getObject(), time);
    }


    /**
     * 自定义时间
     *
     * @param key
     * @param t
     * @param clazz
     * @param time
     * @param <T>
     * @return
     */
    public <T> T globalCache(String key, T t, Class<T> clazz, Long time) {
        String cache = get(key);
        if (StringUtils.isNotBlank(cache)) {
            return resultCache(cache, clazz);
        }
        return resultobject(key, t, time);
    }


    private <T> T resultCache(String cache, Class<T> clazz) {
        return JSONObject.parseObject(cache, clazz);
    }

    private <T> T resultobject(String key, T t, Long time) {
        if (Objects.isNull(t)) {
            return null;
        }
        set(key, JSONObject.toJSONString(t), time);
        return t;
    }

    public void put(String key, Object hasKey, Object hasVal) {
        redisTemplate.opsForHash().put(key, hasKey, hasVal);
    }

    public void putAll(String key, Map<Object, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public Map<Object, Object> getHasAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Object getHas(String key, Object hasKey) {
        return redisTemplate.opsForHash().get(key, hasKey);
    }

    public <T> T getHas(String key, Object hasKey, Class<T> clazz) {
        return JSONObject.parseObject(JSONObject.toJSONString(getHas(key, hasKey)), clazz);
    }

    public Long hasSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    public void removeHas(String key, Object hasKey) {
        redisTemplate.opsForHash().delete(key, hasKey);
    }
}

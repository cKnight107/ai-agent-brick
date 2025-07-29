package com.agent.brick.constants;

/**
 * 全局时间 常量 毫秒级
 * @author cKnight
 * @since 2024/6/11
 */
public interface TimeConstant {

    /**
     * 半小时
     */
    Long HALF_HOUR = 1000L * 60 * 30;

    /**
     * 一小时
     */
    Long ONE_HOUR = 1000L * 60 * 60;

    /**
     * 一秒钟
     */
    Long ONE_SEND = 1000L;

    /**
     * 一分钟
     */
    Long ONE_MIN = 1000L* 60;

    /**
     * 一天
     */
    Long ONE_DAY = ONE_HOUR * 24;

    /**
     * 10分钟
     */
    Long TEN_MIN = 1000L * 60 * 10;

}

package com.fenqile.delayqueue.consumer.config;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author mattguo
 * @version 2018年07月18日 17:36
 */
public class DelayQueueMessageRetryConfig {

    /**
     * 延迟消息级别，默认分为18级（级别从1开始）
     */
    public static final List<String> delayLevelList;

    /**
     * 消息初始化默认重试次数，最多18次，和上面级别对应
     */
    public static final Integer defaultRetryTimes;

    static {
        delayLevelList = Lists.newArrayList("1s", "5s", "10s", "30s", "1m", "2m", "3m",
                "4m", "5m", "6m", "7m", "8m", "9m", "10m", "20m", "30m", "1h", "2h");
        defaultRetryTimes = delayLevelList.size();
    }

    public static Long getNextLongTimeByLevel(Integer level) {
        String delayTime = delayLevelList.get(defaultRetryTimes - level);
        Long numerical = Long.parseLong(delayTime.substring(0, delayTime.length() - 1));
        String unit = delayTime.substring(delayTime.length() - 1);
        Long defaultDelayTime = 5000L;
        switch (unit) {
            case "s":
                defaultDelayTime = numerical * 1000;
                break;
            case "m":
                defaultDelayTime = numerical * 60 * 1000;
                break;
            case "h":
                defaultDelayTime = numerical * 60 * 60 * 1000;
                break;
        }
        return defaultDelayTime;
    }

    public static Long getNextLongTimeByLevel(List<String> delayLevelList,Integer level) {
        int size = delayLevelList.size();

        //防止溢出
        if(level > size){
            level = size;
        }

        String delayTime = delayLevelList.get(size - level);
        Long numerical = Long.parseLong(delayTime.substring(0, delayTime.length() - 1));
        String unit = delayTime.substring(delayTime.length() - 1);
        Long defaultDelayTime = 5000L;
        switch (unit) {
            case "s":
                defaultDelayTime = numerical * 1000;
                break;
            case "m":
                defaultDelayTime = numerical * 60 * 1000;
                break;
            case "h":
                defaultDelayTime = numerical * 60 * 60 * 1000;
                break;
        }
        return defaultDelayTime;
    }
}

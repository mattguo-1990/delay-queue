package com.fenqile.delayqueue.strategy;

import lombok.Data;

/**
 * @author mattguo
 * @version 2018年07月15日 21:21
 */

/**
 * 重试策略
 */

@Data
public class RetryStrategy {
    /**
     * 重试次数
     */
    private Integer retryTimes;

    /**
     * 重试时间间隔
     */
    private Long interval;
}

package com.fenqile.delayqueue.redis.bean;

import lombok.Data;

/**
 * @author mattguo
 * @version 2018年07月10日 21:11
 */
@Data
public class DelayQueueJob {
    /**
     * 延迟任务的唯一标识，用于检索任务
     */
    private String jobId;

    /**
     * 任务的权重
     */
    private Double score;
}

package com.fenqile.delayqueue.redis.config;

/**
 * @author mattguo
 * @version 2018年07月12日 17:06
 */
public class RedisConfig {
    /**
     * 工作队列，消费者消费消息时同时放进工作队列
     */
    public static final String WORK_QUEUE_NAME = "consume_message_workqueue";

    /**
     * 记录异常消息队列名称
     */
    public static final String ERROR_MESSAGE_QUEUE_NAME = "consume_error_message_workqueue";

    /**
     * 消息key前缀
     */
    public static final String DELAY_JOB_POOL_PREFIX = "DELAY_JOB_";

    /**
     * 消息桶名前缀
     */
    public static final String BUCKET_NAME_PREFIX = "BUCKET_NAME_";
}

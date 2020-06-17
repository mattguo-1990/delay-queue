package com.fenqile.delayqueue.service;


import com.fenqile.delayqueue.redis.bean.DelayQueueErrorMessage;
import com.fenqile.delayqueue.redis.bean.Message;
import com.fenqile.delayqueue.strategy.RetryStrategy;
import com.fenqile.delayqueue.redis.bean.DelayQueueMessage;
import com.fenqile.delayqueue.redis.bean.RetryErrorRecord;

/**
 * @author mattguo
 * @version 2018年07月10日 15:18
 */
public abstract interface DelayQueueMessageService {

    /**
     * @param message     客户端发送延迟任务消息
     * @param executeTime 消息执行时间
     * @return messageId  返回消息ID
     */
    public abstract String addDelayMessage(String topic, Message message, Long executeTime);

    /**
     * 添加需要重发的消息
     *
     * @param message
     * @param retryStrategy
     * @return
     */
    public abstract String addRetryMessage(String topic, Message message, RetryStrategy retryStrategy);

    public abstract String addRetryMessage(String msgId, String topic, String message, RetryStrategy retryStrategy, String bizType);

    /**
     * 添加需要重发的消息（存在DB里面的异常消息记录）
     *
     * @param topic
     * @param msgId
     * @param message
     * @return
     */
    public abstract String addRetryErrorMessage(String topic, String msgId, String message);

    /**
     * 将延时队列错误信息保存到redis
     *
     * @param delayQueueErrorMessage
     */
    public abstract void addDelayQueueErrorMessage(DelayQueueErrorMessage delayQueueErrorMessage);

    /**
     * 根据messageId 获取消息
     *
     * @param messageId
     * @return
     */
    public abstract String getMessage(String messageId);

    /**
     * 根据分片item获取对应bucket的第一个messageId
     *
     * @param shard
     * @return
     */
    public abstract String getMessageId(String shard);

    /**
     * 根据分片item从对应的桶里面移除messageId
     *
     * @param shardItem
     * @param jobId
     * @return
     */
    public abstract Long removeMessageFromBucket(String shardItem, String jobId);

    /**
     * 根据messageId删除消息
     *
     * @param messageId
     */
    public abstract Long removeMessage(String messageId);

    /**
     * 将任务推送到redis List 以便消费者进行消费
     *
     * @param delayQueueMessage
     * @param shardItem
     * @return
     */
    public abstract boolean pushMessageToRedisList(DelayQueueMessage delayQueueMessage, String shardItem);
}

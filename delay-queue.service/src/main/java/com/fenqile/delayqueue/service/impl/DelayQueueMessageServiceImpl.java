package com.fenqile.delayqueue.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fenqile.delayqueue.exception.DelayQueueException;
import com.fenqile.delayqueue.redis.bean.DelayQueueErrorMessage;
import com.fenqile.delayqueue.redis.bean.Message;
import com.fenqile.delayqueue.redis.common.DelayQueueTool;
import com.fenqile.delayqueue.strategy.RetryStrategy;
import com.fenqile.delayqueue.config.MessageConfig;
import com.fenqile.delayqueue.config.ShardConfig;
import com.fenqile.delayqueue.generator.SeqNoGenerator;
import com.fenqile.delayqueue.redis.bean.DelayQueueMessage;
import com.fenqile.delayqueue.redis.bean.DelayQueueJob;
import com.fenqile.delayqueue.redis.config.RedisConfig;
import com.fenqile.delayqueue.redis.utils.DelayQueueRedisUtils;
import com.fenqile.delayqueue.service.DelayQueueMessageService;
import com.fenqile.delayqueue.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.fenqile.delayqueue.exception.DelayQueueErrorCode.MESSAGE_INVALID;
import static com.fenqile.delayqueue.exception.DelayQueueErrorCode.MESSAGE_TOPIC_EMPTY_INVALID;

/**
 * @author mattguo
 * @version 2018年07月10日 15:23
 */

@Component
public class DelayQueueMessageServiceImpl implements DelayQueueMessageService, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelayQueueMessageService.class);
    private static final String VERSION = "1.0.0-SNAPSHOT";

    /**
     * @param message     客户端发送消息
     * @param executeTime 消息执行时间
     * @return messageId 返回消息ID
     */
    @Override
    public String addDelayMessage(String topic, Message message, Long executeTime) {
        LOGGER.debug("message:{},executeTime:{}", message, executeTime);
        messageCheck(topic, message);
        try {
            DelayQueueMessage delayQueueMessage = buildDelayMessage(topic, message, executeTime);
            Integer shardNo = getShardNo(delayQueueMessage.getId());
            LOGGER.debug("===================> generated ID :{}", delayQueueMessage.getId());
            LOGGER.debug("===================> shardNo :{}", shardNo);
            return !this.addJob(delayQueueMessage) ? null : delayQueueMessage.getId();
        } catch (Exception e) {
            LOGGER.error("addMessage error ! message:{} ,executeTime: {} ", message, executeTime, e);
            return null;
        }
    }

    private void messageCheck(String topic, Message message) {
        if (StringUtils.isEmpty(topic)) {
            throw new DelayQueueException(MESSAGE_TOPIC_EMPTY_INVALID);
        }
        if (message == null || StringUtils.isEmpty(message.getBody()) || message.getBody().length() > 4000) {
            throw new DelayQueueException(MESSAGE_INVALID);
        }
    }

    @Override
    public String addRetryMessage(String topic, Message message, RetryStrategy retryStrategy) {
        LOGGER.debug("message:{}", message);
        try {
            DelayQueueMessage delayQueueMessage = buildRetryMessage(topic, message, retryStrategy);
            Integer shardNo = getShardNo(delayQueueMessage.getId());
            LOGGER.debug("===================> generated ID :{}", delayQueueMessage.getId());
            LOGGER.debug("===================> shardNo :{}", shardNo);
            return !this.addJob(delayQueueMessage) ? null : delayQueueMessage.getId();
        } catch (Exception e) {
            LOGGER.error("addMessage error ! message:{}", message, e);
            return null;
        }
    }

    @Override
    public String addRetryMessage(String msgId, String topic, String message, RetryStrategy retryStrategy, String bizType) {
        Message messageBean = new Message();
        messageBean.setMsgId(msgId);
        messageBean.setBody(message);
        messageBean.setBizType(bizType);
        return addRetryMessage(topic, messageBean, retryStrategy);
    }

    @Override
    public String addRetryErrorMessage(String topic, String msgId, String message) {
        LOGGER.debug("message:{}", message);
        try {
            DelayQueueMessage delayQueueMessage = new DelayQueueMessage();
            delayQueueMessage.setId(msgId);
            delayQueueMessage.setBody(message);
            delayQueueMessage.setCreateTime(new Date().getTime());
            delayQueueMessage.setRetryTimes(1);  //只重试一次
            delayQueueMessage.setExcuteTime(DateUtil.now());
            delayQueueMessage.setTopic(topic);
            delayQueueMessage.setType(MessageConfig.MESSAGE_RETRY);
            delayQueueMessage.setIsFromDB(true);
            Integer shardNo = getShardNo(delayQueueMessage.getId());
            LOGGER.debug("===================> generated ID :{}", delayQueueMessage.getId());
            LOGGER.debug("===================> shardNo :{}", shardNo);
            return !this.addJob(delayQueueMessage) ? null : delayQueueMessage.getId();
        } catch (Exception e) {
            LOGGER.error("addMessage error ! message:{}", message, e);
            return null;
        }
    }

    @Override
    public void addDelayQueueErrorMessage(DelayQueueErrorMessage delayQueueErrorMessage) {
        Assert.notNull(delayQueueErrorMessage, "delayQueueErrorMessage can't be null");

        DelayQueueRedisUtils.lpush(RedisConfig.ERROR_MESSAGE_QUEUE_NAME, JSONObject.toJSONString(delayQueueErrorMessage));
    }

    private Boolean addJob(DelayQueueMessage delayQueueMessage) {

        assertNull(delayQueueMessage);

        Integer shardNo = getShardNo(delayQueueMessage.getId());
        List<String> keys = new ArrayList<>();
        List<String> args = new ArrayList<>();

        keys.add(RedisConfig.DELAY_JOB_POOL_PREFIX + delayQueueMessage.getId());
        keys.add(RedisConfig.BUCKET_NAME_PREFIX + shardNo);
        keys.add(delayQueueMessage.getId());

        args.add(JSONObject.toJSONString(delayQueueMessage));
        args.add(String.valueOf(delayQueueMessage.getExcuteTime()));

        Long result = (Long) DelayQueueRedisUtils.addJobToBucket(keys, args);

        if (result == 1L) {
            return true;
        }

        LOGGER.error("addJob return 0 ");
        return false;
    }

    @Override
    public String getMessage(String messageId) {
        String message = DelayQueueRedisUtils.getValue(RedisConfig.DELAY_JOB_POOL_PREFIX + messageId);
        LOGGER.debug("get message from redis,key:{},value:{}", RedisConfig.DELAY_JOB_POOL_PREFIX + messageId, message);
        return message;
    }

    @Override
    public String getMessageId(String shard) {
        DelayQueueJob job = DelayQueueRedisUtils.getFromBucket(RedisConfig.BUCKET_NAME_PREFIX + shard);
        if (job != null) {
            LOGGER.debug("get job from bucketName:{},job id:{}", RedisConfig.BUCKET_NAME_PREFIX + shard, job.getJobId());
            return job.getJobId();
        }
        return null;
    }

    @Override
    public Long removeMessageFromBucket(String bucketName, String jobId) {
        return DelayQueueRedisUtils.removeFromBucket(bucketName, jobId);
    }

    @Override
    public Long removeMessage(String messageId) {
        return DelayQueueRedisUtils.del(RedisConfig.DELAY_JOB_POOL_PREFIX + messageId);
    }

    @Override
    public boolean pushMessageToRedisList(DelayQueueMessage delayQueueMessage, String shardItem) {

        assertNull(delayQueueMessage);

        List<String> keys = new ArrayList<>();
        List<String> args = new ArrayList<>();

        keys.add(delayQueueMessage.getTopic());
        keys.add(RedisConfig.BUCKET_NAME_PREFIX + shardItem);

        args.add(delayQueueMessage.getId() + MessageConfig.SEPRATOR + DateUtil.addSeconds(delayQueueMessage.getExcuteTime(), MessageConfig.REPUSH_JOB_DELAY_SECONDS));
        args.add(delayQueueMessage.getId());

        LOGGER.debug("push message into redis list,list Name:{}", delayQueueMessage.getTopic());
        Long result = (Long) DelayQueueRedisUtils.pushJobToRedisList(keys, args);

        if (result == 1L) {
            return true;
        }

        LOGGER.error("pushMessageToRedisList return 0 ");
        return false;
    }

    /**
     * 获取生成ID对应的shard片
     *
     * @param generatId
     * @return
     */
    public Integer getShardNo(String generatId) {
        generatId = generatId.substring(ShardConfig.timeSubStart, ShardConfig.timeSubEnd);
        Integer timeInteger = Integer.parseInt(generatId);
        return timeInteger % ShardConfig.shardNum + 1;
    }

    public DelayQueueMessage buildDelayMessage(String topic, Message message, Long executeTime) {
        DelayQueueMessage delayQueueMessage = new DelayQueueMessage();
        delayQueueMessage.setId(SeqNoGenerator.genId());
        delayQueueMessage.setBody(message.getBody());
        delayQueueMessage.setBizType(message.getBizType());
        delayQueueMessage.setCreateTime(new Date().getTime());
        delayQueueMessage.setExcuteTime(executeTime);
        delayQueueMessage.setTopic(DelayQueueTool.getEnv() + topic);
        delayQueueMessage.setType(MessageConfig.DELAY);
        return delayQueueMessage;
    }

    public DelayQueueMessage buildRetryMessage(String topic, Message message, RetryStrategy retryStrategy) {
        DelayQueueMessage delayQueueMessage = new DelayQueueMessage();
        delayQueueMessage.setId(message.getMsgId());
        delayQueueMessage.setBody(message.getBody());
        delayQueueMessage.setBizType(message.getBizType());
        delayQueueMessage.setBizType(message.getBizType());
        delayQueueMessage.setCreateTime(new Date().getTime());
        delayQueueMessage.setRetryTimes(retryStrategy.getRetryTimes());
        delayQueueMessage.setInterval(retryStrategy.getInterval());
        delayQueueMessage.setExcuteTime(DateUtil.addMillSeconds(new Date(), retryStrategy.getInterval().intValue()));
        delayQueueMessage.setTopic(DelayQueueTool.getEnv() + topic);
        delayQueueMessage.setType(MessageConfig.MESSAGE_RETRY);
        return delayQueueMessage;
    }

    public void assertNull(DelayQueueMessage delayQueueMessage) {
        Assert.notNull(delayQueueMessage.getId(), "message Id can't be null");
        Assert.notNull(delayQueueMessage.getBody(), "message body can't be null");
        Assert.notNull(delayQueueMessage.getTopic(), "message topic can't be null");
        Assert.notNull(delayQueueMessage.getExcuteTime(), "message executeTime can't be null");
        Assert.notNull(delayQueueMessage.getCreateTime(), "message createTime can't be null");
        Assert.notNull(delayQueueMessage.getType(), "message type can't be null");
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}

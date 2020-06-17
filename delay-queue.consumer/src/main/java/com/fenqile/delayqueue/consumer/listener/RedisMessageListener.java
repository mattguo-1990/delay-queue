package com.fenqile.delayqueue.consumer.listener;

import com.alibaba.fastjson.JSONObject;
import com.fenqile.delayqueue.config.MessageConfig;
import com.fenqile.delayqueue.consumer.config.DelayQueueMessageRetryConfig;
import com.fenqile.delayqueue.consumer.cosumer.RedisMessageConsumer;
import com.fenqile.delayqueue.consumer.enums.RedisMessageStatus;
import com.fenqile.delayqueue.exception.DelayQueueException;
import com.fenqile.delayqueue.redis.bean.DelayQueueErrorMessage;
import com.fenqile.delayqueue.redis.bean.DelayQueueMessage;
import com.fenqile.delayqueue.redis.bean.Message;
import com.fenqile.delayqueue.redis.common.DelayQueueTool;
import com.fenqile.delayqueue.redis.config.RedisConfig;
import com.fenqile.delayqueue.redis.utils.DelayQueueRedisUtils;
import com.fenqile.delayqueue.service.DelayQueueMessageService;
import com.fenqile.delayqueue.strategy.RetryStrategy;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

import static com.fenqile.delayqueue.exception.DelayQueueErrorCode.SYSTEM_ERROR;
import static com.fenqile.delayqueue.exception.DelayQueueErrorCode.TOPIC_NOT_FOUND;
import static com.fenqile.delayqueue.redis.config.RedisConfig.WORK_QUEUE_NAME;

/**
 * @author mattguo
 * @version 2018年07月12日 15:56
 */
@Slf4j
public abstract class RedisMessageListener {

    @Autowired
    private DelayQueueMessageService delayQueueMessageService;

    @Autowired
    private RedisMessageConsumer redisMessageConsumer;

    /**
     * 客户端listener监听主题名称
     */
    private String topic;

    /**
     * 消息失败重试策略
     */
    private List<String> delayLevelList;

    public RedisMessageListener(String topic) {
        this.topic = DelayQueueTool.getEnv() + topic;
    }

    public RedisMessageListener(String topic, List<String> delayLevelList) {
        this.topic = DelayQueueTool.getEnv() + topic;
        this.delayLevelList = delayLevelList;
    }

    public void onEvent(String source) {

        String jobId = source.split(MessageConfig.SEPRATOR)[0];
        if (StringUtils.isNotEmpty(jobId)) {

            String message = delayQueueMessageService.getMessage(jobId);
            if (StringUtils.isEmpty(message)) {
                //如果消息为空，从工作队列中移除
                log.info("jobId:{} is not exist in Redis Pool !", jobId);
                DelayQueueRedisUtils.removeFromWorkQueue(WORK_QUEUE_NAME, jobId);
                return;
            }

            DelayQueueMessage delayQueueMessage = JSONObject.parseObject(message, DelayQueueMessage.class);

            Integer leftRetryTimes = 0;
            MessageReturn messageReturn = null;

            try {
                Message messagebean = new Message();
                messagebean.setMsgId(delayQueueMessage.getId());
                messagebean.setBody(delayQueueMessage.getBody());
                messagebean.setBizType(delayQueueMessage.getBizType());
                messageReturn = consumeMessage(messagebean);
            } catch (Exception e) {
                log.error("consumeMessage error !", e);
                messageReturn = RedisMessageUtils.failWithErrorMessage(e.toString());
                //记录异常到redis
                addErrorMessageToRedis(delayQueueMessage.getId(), delayQueueMessage.getBizType(), delayQueueMessage.getBody(), delayQueueMessage.getTopic(), getDefaultRetryTimes(), "FAIL", messageReturn.getErrorMessage());
                return;
            }

            //已经消费此job，删除
            deleteJob(jobId, source);

            switch (delayQueueMessage.getType()) {
                case MessageConfig.MESSAGE_RETRY:
                    leftRetryTimes = delayQueueMessage.getRetryTimes();
                    if (leftRetryTimes <= 0) {
                        break;
                    }

                    if (messageReturn.getRedisMessageStatus() != RedisMessageStatus.SUCCESS) {
                        if (leftRetryTimes - 1 <= 0) {
                            //记录异常到redis
                            addErrorMessageToRedis(delayQueueMessage.getId(), delayQueueMessage.getBizType(), delayQueueMessage.getBody(), delayQueueMessage.getTopic(), getDefaultRetryTimes(), "FAIL", messageReturn.getErrorMessage());
                        } else {
                            RetryStrategy retryStrategy = new RetryStrategy();
                            retryStrategy.setRetryTimes(leftRetryTimes - 1);

                            if (delayLevelList == null && delayLevelList.size() <= 0) {
                                retryStrategy.setInterval(DelayQueueMessageRetryConfig.getNextLongTimeByLevel(retryStrategy.getRetryTimes()));
                            } else {
                                retryStrategy.setInterval(DelayQueueMessageRetryConfig.getNextLongTimeByLevel(delayLevelList, retryStrategy.getRetryTimes()));
                            }

                            delayQueueMessageService.addRetryMessage(delayQueueMessage.getId(), delayQueueMessage.getTopic(), delayQueueMessage.getBody(), retryStrategy, delayQueueMessage.getBizType());
                        }
                    } else {
                        if (delayQueueMessage.getIsFromDB()) {
                            addErrorMessageToRedis(delayQueueMessage.getId(), delayQueueMessage.getBizType(), delayQueueMessage.getBody(), delayQueueMessage.getTopic(), DelayQueueMessageRetryConfig.defaultRetryTimes, "SUCCESS", "SUCCESS");
                        }
                    }

                    break;

                case MessageConfig.DELAY:

                    if (messageReturn.getRedisMessageStatus() == RedisMessageStatus.FAIL_NO_RETRY) {
                        //记录异常到redis
                        addErrorMessageToRedis(delayQueueMessage.getId(), delayQueueMessage.getBizType(), delayQueueMessage.getBody(), delayQueueMessage.getTopic(), getDefaultRetryTimes(), "FAIL", messageReturn.getErrorMessage());
                    }

                    //消费失败重试
                    if (messageReturn.getRedisMessageStatus() == RedisMessageStatus.FAIL_RETRY) {
                        RetryStrategy retryStrategy = new RetryStrategy();

                        if (delayLevelList == null && delayLevelList.size() <= 0) {
                            retryStrategy.setRetryTimes(DelayQueueMessageRetryConfig.defaultRetryTimes);
                            retryStrategy.setInterval(DelayQueueMessageRetryConfig.getNextLongTimeByLevel(DelayQueueMessageRetryConfig.defaultRetryTimes));
                        } else {
                            retryStrategy.setRetryTimes(delayLevelList.size());
                            retryStrategy.setInterval(DelayQueueMessageRetryConfig.getNextLongTimeByLevel(delayLevelList, delayLevelList.size()));
                        }

                        delayQueueMessageService.addRetryMessage(delayQueueMessage.getId(), delayQueueMessage.getTopic(), delayQueueMessage.getBody(), retryStrategy, delayQueueMessage.getBizType());
                    }

                    break;
                default:
                    break;
            }
        }
    }

    public int getDefaultRetryTimes() {
        return delayLevelList == null ? DelayQueueMessageRetryConfig.defaultRetryTimes : delayLevelList.size();
    }

    public void deleteJob(String jobId, String source) {
        List<String> keys = Lists.newArrayList(RedisConfig.DELAY_JOB_POOL_PREFIX + jobId, WORK_QUEUE_NAME, source);
        List<String> args = Lists.newArrayList();

        Long result = (Long) DelayQueueRedisUtils.delJob(keys, args);
        log.info(" delJob result ----------------> {} ,jobId : {}", result, jobId);
    }

    public void addErrorMessageToRedis(String msgId, String bizType, String body, String topic, Integer excuteTimes, String executeState, String errorMsg) {
        delayQueueMessageService.addDelayQueueErrorMessage(buildErrorMessage(msgId, bizType, body, topic, excuteTimes, executeState, errorMsg));
    }

    public DelayQueueErrorMessage buildErrorMessage(String msgId, String bizType, String message, String topic, Integer excuteTimes, String executeState, String errorMsg) {
        Date now = new Date();
        DelayQueueErrorMessage delayQueueErrorMessage = new DelayQueueErrorMessage();
        delayQueueErrorMessage.setMsgId(msgId);
        delayQueueErrorMessage.setMessage(message);
        delayQueueErrorMessage.setTopic(topic);
        delayQueueErrorMessage.setBizType(bizType);
        delayQueueErrorMessage.setExcuteTimes(excuteTimes);
        delayQueueErrorMessage.setExcuteState(executeState);
        delayQueueErrorMessage.setLastExcuteTime(now);
        delayQueueErrorMessage.setCreateTime(now);
        delayQueueErrorMessage.setModifyTime(now);
        delayQueueErrorMessage.setErrorMsg(errorMsg);
        return delayQueueErrorMessage;
    }

    public abstract MessageReturn consumeMessage(Message message);

    @PostConstruct
    public void init() {
        if (StringUtils.isEmpty(topic)) {
            log.error("listener : {} get topic from constructor is null ", this.getClass().getName());
            throw new DelayQueueException(TOPIC_NOT_FOUND);
        }
        try {
            redisMessageConsumer.addListener(topic, this);
        } catch (Exception e) {
            log.error(" add redisMessageListener error ! {} ", e);
            throw new DelayQueueException(SYSTEM_ERROR);
        }
    }
}

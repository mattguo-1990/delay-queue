package com.fenqile.delayqueue.consumer.listener;

import com.fenqile.delayqueue.consumer.enums.RedisMessageStatus;

/**
 * @author mattguo
 * @version 2018年07月20日 18:00
 */
public class MessageReturn {

    private String errorMessage;

    private RedisMessageStatus redisMessageStatus;

    public MessageReturn(RedisMessageStatus redisMessageStatus){
        this.redisMessageStatus = redisMessageStatus;
    }

    public MessageReturn(RedisMessageStatus redisMessageStatus,String errorMessage){
        this.redisMessageStatus = redisMessageStatus;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public RedisMessageStatus getRedisMessageStatus() {
        return redisMessageStatus;
    }

    public void setRedisMessageStatus(RedisMessageStatus redisMessageStatus) {
        this.redisMessageStatus = redisMessageStatus;
    }
}

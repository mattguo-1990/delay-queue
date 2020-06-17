package com.fenqile.delayqueue.consumer.listener;

import com.fenqile.delayqueue.consumer.enums.RedisMessageStatus;

/**
 * @author mattguo
 * @version 2018年07月20日 18:27
 */
public class RedisMessageUtils {

    public static MessageReturn success(){
        return new MessageReturn(RedisMessageStatus.SUCCESS);
    }

    public static MessageReturn retry(){
        return new MessageReturn(RedisMessageStatus.FAIL_RETRY);
    }

    public static MessageReturn retryWithErrorMessage(String errorMessage){
        return new MessageReturn(RedisMessageStatus.FAIL_RETRY,errorMessage);
    }

    public static MessageReturn fail(){
        return new MessageReturn(RedisMessageStatus.FAIL_NO_RETRY);
    }

    public static MessageReturn failWithErrorMessage(String errorMessage){
        return new MessageReturn(RedisMessageStatus.FAIL_NO_RETRY,errorMessage);
    }
}

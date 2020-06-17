package com.fenqile.delayqueue.redis.bean;

/**
 * @author mattguo
 * @version 2018年07月10日 16:23
 */

import lombok.Data;

/**
 * 延迟队列消息体
 */
@Data
public class DelayQueueMessage {

    private String id; //消息ID

    private String body; //消息体

    private String topic; //消息主题 用于投放消息  Retry消息不需要投放

    private Integer retryTimes; //重试次数

    private Long interval; //间隔时间

    private Long excuteTime; //消息执行时间

    private Long createTime; //消息创建时间

    private String type; // "Delay ,messageRetry ,methodRetry"

    private String bizType; //业务类型：客户端使用

    private Boolean isFromDB = false; // 是否是从DB捞取出来重试的异常消息
}

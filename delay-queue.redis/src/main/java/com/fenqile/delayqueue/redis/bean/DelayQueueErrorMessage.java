package com.fenqile.delayqueue.redis.bean;

import lombok.Data;

import java.util.Date;

/**
 * @author mattguo
 * @version 2018年07月23日 10:49
 */
@Data
public class DelayQueueErrorMessage {
    private String msgId;
    private String message;
    private String topic;
    private String bizType;
    private Integer excuteTimes;
    private String excuteState;
    private String errorMsg;
    private String errorCode;
    private Date lastExcuteTime;
    private Date createTime;
    private Date modifyTime;
}

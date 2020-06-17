package com.fenqile.delayqueue.redis.bean;

import lombok.Data;

/**
 * @author mattguo
 * @version 2018年07月15日 21:34
 */

@Data
public class RetryErrorRecord {

    private String serviceName;
    private String methodName;
    private String params;
    private String appName;
    private String excuteTimes;
    private String excuteState;
}


package com.fenqile.delayqueue.config;

/**
 * @author mattguo
 * @version 2018年07月16日 10:19
 */
public class MessageConfig {

    //延迟消息
    public static final String DELAY = "delay";

    //重试消息
    public static final String MESSAGE_RETRY = "messageRetry";

    public static final String METHOD_RETRY = "methodRetry";

    //RPOPLPUSH工作队列重发JOB延迟秒数,设置时间间隔，减少重复发送几率
    public static final Integer REPUSH_JOB_DELAY_SECONDS = 120;

    public static final String SEPRATOR = ",";
}

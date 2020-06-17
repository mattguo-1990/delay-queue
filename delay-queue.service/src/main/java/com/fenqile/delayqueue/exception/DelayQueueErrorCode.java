package com.fenqile.delayqueue.exception;

import com.fenqile.exception.FqlErrorCode;

import java.io.Serializable;

/**
 * @author mattguo
 * @version 2018年09月17日 10:28
 */
public enum DelayQueueErrorCode implements FqlErrorCode, Serializable {

    SYSTEM_ERROR("22001001", "系统异常"),
    MESSAGE_INVALID("22001002", "message不能为空，并且长度不能大于4000"),
    MESSAGE_TOPIC_EMPTY_INVALID("22001003", "消息主题不能为空"),
    TOPIC_NOT_FOUND("22001004", "在客户端监听器的构造器中未找到消息主题"),
    ;

    private String code;

    private String msg;

    DelayQueueErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String code() {
        return msg;
    }

    @Override
    public String msg() {
        return msg;
    }
}

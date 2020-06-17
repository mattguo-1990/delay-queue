package com.fenqile.delayqueue.consumer.enums;

/**
 * @author mattguo
 * @version 2018年07月13日 11:21
 */
public enum RedisMessageStatus {
    SUCCESS(1,"成功"),
    FAIL_NO_RETRY(2,"失败不重试"),
    FAIL_RETRY(3,"失败重试"),
    EXCEPTION(4,"失败");

    private int code;

    private String message;

    RedisMessageStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

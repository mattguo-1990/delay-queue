package com.fenqile.delayqueue.redis.common;

import org.apache.commons.lang3.StringUtils;

/**
 * @author mattguo
 * @version 2018年09月19日 17:46
 */
public class DelayQueueTool {

    public static String getEnv() {
        return StringUtils.isBlank(System.getProperty("dubbo.application.environment")) ? "" : System.getProperty("dubbo.application.environment");
    }
}

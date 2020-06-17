package com.fenqile.delayqueue.consumer.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author mattguo
 * @version 2018年07月12日 15:54
 */
public class RedisMessageEvent extends ApplicationEvent {

    public RedisMessageEvent(String message) {
        super(message);
    }
}

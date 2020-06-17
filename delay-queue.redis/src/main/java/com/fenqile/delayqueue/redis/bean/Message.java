package com.fenqile.delayqueue.redis.bean;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author mattguo
 * @version 2018年08月20日 16:10
 */

@Data
public class Message {
    /**
     * 消息ID
     */
    private String msgId;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 消息体
     */
    private String body;
}

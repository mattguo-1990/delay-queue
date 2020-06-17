package com.fenqile.delayqueue.job.domain.request;

import lombok.Data;

/**
 * @author mattguo
 * @version 2018年08月07日 19:18
 */
@Data
public class RetryErrorMessageRequest {
    private String msgId;
    private String memo;
}

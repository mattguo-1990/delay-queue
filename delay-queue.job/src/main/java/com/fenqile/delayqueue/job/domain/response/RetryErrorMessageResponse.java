package com.fenqile.delayqueue.job.domain.response;

import lombok.Data;

/**
 * @author mattguo
 * @version 2018年08月07日 19:18
 */
@Data
public class RetryErrorMessageResponse {
    private Integer resultCode;
    private String resultInfo;
}

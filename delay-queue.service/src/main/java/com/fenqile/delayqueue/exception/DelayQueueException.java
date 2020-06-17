package com.fenqile.delayqueue.exception;

import com.fenqile.exception.FqlErrorCode;
import com.fenqile.exception.FqlException;

/**
 * @author mattguo
 * @version 2018年08月07日 21:33
 */
public class DelayQueueException extends FqlException {

    public DelayQueueException(FqlErrorCode message) {
        super(message);
    }
}

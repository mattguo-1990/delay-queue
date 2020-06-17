package com.fenqile.delayqueue.job.service;

import com.fenqile.delayqueue.job.domain.request.RetryErrorMessageRequest;
import com.fenqile.delayqueue.job.domain.response.RetryErrorMessageResponse;

public interface RetryErrorMessageService {

    public RetryErrorMessageResponse retryErrorMessage(RetryErrorMessageRequest retryErrorMessageRequest);
}

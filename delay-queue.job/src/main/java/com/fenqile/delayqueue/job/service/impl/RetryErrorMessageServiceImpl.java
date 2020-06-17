package com.fenqile.delayqueue.job.service.impl;

import com.fenqile.delayqueue.dao.db.dao.DelayMessageErrorDao;
import com.fenqile.delayqueue.dao.db.domain.DelayMessageError;
import com.fenqile.delayqueue.job.domain.request.RetryErrorMessageRequest;
import com.fenqile.delayqueue.job.domain.response.RetryErrorMessageResponse;
import com.fenqile.delayqueue.job.service.RetryErrorMessageService;
import com.fenqile.delayqueue.job.util.CollectionUtil;
import com.fenqile.delayqueue.service.DelayQueueMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 人工执行异常消息
 */
@Slf4j
@Service("retryErrorMessageService")
public class RetryErrorMessageServiceImpl implements RetryErrorMessageService {

    @Autowired
    private DelayMessageErrorDao delayMessageErrorDao;

    @Autowired
    private DelayQueueMessageService delayQueueMessageService;

    @Override
    public RetryErrorMessageResponse retryErrorMessage(RetryErrorMessageRequest retryErrorMessageRequest) {
        if (retryErrorMessageRequest == null) {
            throw new IllegalArgumentException("retryErrorMessageRequest can't be null");
        }

        if (StringUtils.isEmpty(retryErrorMessageRequest.getMsgId())) {
            throw new IllegalArgumentException("retryErrorMessageRequest msgId can't be null");
        }

        if (StringUtils.isEmpty(retryErrorMessageRequest.getMemo())) {
            throw new IllegalArgumentException("retryErrorMessageRequest memo can't be null");
        }

        RetryErrorMessageResponse response = new RetryErrorMessageResponse();
        response.setResultCode(0);

        try {
            DelayMessageError delayMessageErrorRecord = CollectionUtil.
                    getFirstElement(delayMessageErrorDao.selectByMsgId(retryErrorMessageRequest.getMsgId()));

            if (delayMessageErrorRecord == null) {
                response.setResultInfo("retryErrorMessageRequest msgId not found in db !");
                return response;
            }

            delayQueueMessageService.removeMessage(retryErrorMessageRequest.getMsgId());
            delayQueueMessageService.addRetryErrorMessage(delayMessageErrorRecord.getTopic(), delayMessageErrorRecord.getMsgId(), delayMessageErrorRecord.getMessage());
        } catch (Exception e) {
            log.error("retryErrorMessage error: {} !", e.toString());
            response.setResultInfo(e.toString());
        }

        return response;
    }
}

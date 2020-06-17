package com.fenqile.delayqueue.job.job;

import com.alibaba.fastjson.JSONObject;
import com.fenqile.delayqueue.dao.db.dao.DelayMessageErrorDao;
import com.fenqile.delayqueue.dao.db.domain.DelayMessageError;
import com.fenqile.delayqueue.job.config.JobConfig;
import com.fenqile.delayqueue.job.util.CollectionUtil;
import com.fenqile.delayqueue.redis.bean.DelayQueueErrorMessage;
import com.fenqile.delayqueue.redis.config.RedisConfig;
import com.fenqile.delayqueue.redis.utils.DelayQueueRedisUtils;
import com.fenqile.delayqueue.util.DateUtil;
import com.fenqile.dolphin.job.AbstractDolphinJavaJob;
import com.fenqile.dolphin.job.DolphinJobExecutionContext;
import com.fenqile.dolphin.job.DolphinJobReturn;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author mattguo
 * @version 2018年07月23日 11:18
 */

@Slf4j
@Service
public class MoveDelayQueueErrorMessageToDBJob extends AbstractDolphinJavaJob {

    @Autowired
    private DelayMessageErrorDao delayMessageErrorDao;

    @Override
    public DolphinJobReturn handleJob(String jobName, Integer shardItem, String shardParam, DolphinJobExecutionContext dolphinJobExecutionContext) throws InterruptedException {

        log.info("jobName{},shardItem:{} task start !!!", jobName, shardItem);

        doTask(String.valueOf(shardItem));

        log.info("jobName{},shardItem:{} task end !!!", jobName, shardItem);

        return new DolphinJobReturn("我是分片" + shardItem + "的处理结果" + shardParam);
    }

    public void doTask(String shardItem) {

        Long time = DateUtil.addSeconds(new Date(), JobConfig.runSeconds);

        do {
            try {
                String errorMsg = DelayQueueRedisUtils.rpop(RedisConfig.ERROR_MESSAGE_QUEUE_NAME);

                if (StringUtils.isNotEmpty(errorMsg)) {
                    DelayQueueErrorMessage delayQueueErrorMessage = JSONObject.parseObject(errorMsg, DelayQueueErrorMessage.class);

                    log.info("错误消息回调入库,消息内容{}", delayQueueErrorMessage);

                    DelayMessageError delayMessageError = new DelayMessageError();
                    BeanUtils.copyProperties(delayQueueErrorMessage, delayMessageError);

                    DelayMessageError delayMessageErrorRecord = CollectionUtil.
                            getFirstElement(delayMessageErrorDao.selectByMsgId(delayQueueErrorMessage.getMsgId()));

                    if (delayMessageErrorRecord == null) {
                        delayMessageErrorDao.insertSelective(delayMessageError);
                    } else {
                        delayMessageErrorDao.updateByMsgId(delayMessageError);
                    }
                }

            } catch (Exception ex) {
                log.error(ex.toString());
            }

            sleep();
        } while (DateUtil.now() < time);
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(20L);
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }
}

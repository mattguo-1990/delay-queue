package com.fenqile.delayqueue.job.job;

import com.alibaba.fastjson.JSONObject;
import com.fenqile.delayqueue.job.config.JobConfig;
import com.fenqile.delayqueue.redis.bean.DelayQueueMessage;
import com.fenqile.delayqueue.service.DelayQueueMessageService;
import com.fenqile.delayqueue.util.DateUtil;
import com.fenqile.dolphin.job.AbstractDolphinJavaJob;
import com.fenqile.dolphin.job.DolphinJobExecutionContext;
import com.fenqile.dolphin.job.DolphinJobReturn;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author mattguo
 * @version 2018年07月09日 15:28
 */
@Slf4j
@Service
public class ProduceMessagePushJob extends AbstractDolphinJavaJob {

    @Autowired
    private DelayQueueMessageService delayQueueMessageService;

    @Override
    public DolphinJobReturn handleJob(String jobName, Integer shardItem, String shardParam,
                                      DolphinJobExecutionContext shardingContext) {

        log.info("jobName{},shardItem:{} task start !!!", jobName, shardItem);

        doTask(String.valueOf(shardItem));

        log.info("jobName{},shardItem:{} task end !!!", jobName, shardItem);

        return new DolphinJobReturn("我是分片" + shardItem + "的处理结果" + shardParam);
    }

    public void doTask(String shardItem) {

        Long time = DateUtil.addSeconds(new Date(), JobConfig.runSeconds);

        do {
            String jobId = "";

            try {
                jobId = delayQueueMessageService.getMessageId(shardItem);

                if (StringUtils.isEmpty(jobId)) {
                    sleep();
                    continue;
                }

                String message = delayQueueMessageService.getMessage(jobId);
                if (StringUtils.isEmpty(message)) {
                    delayQueueMessageService.removeMessageFromBucket(shardItem, jobId);
                    sleep();
                    continue;
                }

                JSONObject jsonObject = JSONObject.parseObject(message);
                DelayQueueMessage delayQueueMessage = JSONObject.toJavaObject(jsonObject, DelayQueueMessage.class);
                if (delayQueueMessage.getExcuteTime() > System.currentTimeMillis()) {
                    sleep();
                    continue;
                }

                if (delayQueueMessage.getExcuteTime() <= System.currentTimeMillis()) {

                    //延迟消息，交给客户端自行助理
                    delayQueueMessageService.pushMessageToRedisList(delayQueueMessage, shardItem);
                }

            } catch (IllegalArgumentException e) {
                delayQueueMessageService.removeMessageFromBucket(shardItem, jobId);
            } catch (Exception ex) {
                log.error(ex.toString());
            }

            sleep();
        } while (DateUtil.now() < time);
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(2L);
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }
}

package com.fenqile.delayqueue.job.job;

/**
 * @author mattguo
 * @version 2018年07月19日 19:48
 */

import com.fenqile.delayqueue.config.MessageConfig;
import com.fenqile.delayqueue.config.ShardConfig;
import com.fenqile.delayqueue.job.config.JobConfig;
import com.fenqile.delayqueue.redis.config.RedisConfig;
import com.fenqile.delayqueue.redis.utils.DelayQueueRedisUtils;
import com.fenqile.delayqueue.service.DelayQueueMessageService;
import com.fenqile.delayqueue.util.DateUtil;
import com.fenqile.dolphin.job.AbstractDolphinJavaJob;
import com.fenqile.dolphin.job.DolphinJobExecutionContext;
import com.fenqile.dolphin.job.DolphinJobReturn;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 主要用于处于用于客户端进行LPOPRPUSH操作后异常的消息重发
 */

@Slf4j
@Service
public class RePushMessageJob extends AbstractDolphinJavaJob {

    @Autowired
    private DelayQueueMessageService delayQueueMessageService;

    @Override
    public DolphinJobReturn handleJob(String jobName, Integer shardItem, String shardParam, DolphinJobExecutionContext dolphinJobExecutionContext) throws InterruptedException {

        log.info("jobName{},shardItem:{} task start !!!", jobName, shardItem);

        doTask(String.valueOf(shardItem));

        log.info("jobName{},shardItem:{} task end !!!", jobName, shardItem);

        return new DolphinJobReturn("我是分片" + shardItem + "的处理结果" + shardParam);
    }

    public void doTask(String shardItem) {

        Long time = DateUtil.addSeconds(new Date(), JobConfig.runSeconds);
        String message = null;

        do {
            try {
                //获取队列最后一个元素
                message = DelayQueueRedisUtils.lindex(RedisConfig.WORK_QUEUE_NAME, -1);
                if (StringUtils.isNotEmpty(message) && message.contains(MessageConfig.SEPRATOR)) {
                    Long excuteTime = Long.parseLong(message.split(MessageConfig.SEPRATOR)[1]);

                    if (excuteTime < DateUtil.now()) {
                        sleep();
                        continue;
                    }

                    List<String> keys = new ArrayList<>();
                    List<String> args = new ArrayList<>();

                    keys.add(RedisConfig.WORK_QUEUE_NAME);
                    keys.add(RedisConfig.BUCKET_NAME_PREFIX + getRandom());

                    Long result = (Long) DelayQueueRedisUtils.rpopFromListAndaddJobToBucket(keys, args);

                    if (result == 0L) {
                        log.error("rpopFromListAndaddJobToBucket error !");
                    }
                }

                sleep();
            } catch (Exception ex) {
                log.error("message:{},error:{}",message,ex);
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

    public Integer getRandom() {
        Random random = new Random();
        return random.nextInt(ShardConfig.shardNum) + 1;
    }
}

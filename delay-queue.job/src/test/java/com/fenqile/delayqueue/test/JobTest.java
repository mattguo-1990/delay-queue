package com.fenqile.delayqueue.test;

import com.alibaba.fastjson.JSONObject;
import com.fenqile.delayqueue.config.ShardConfig;
import com.fenqile.delayqueue.job.job.MoveDelayQueueErrorMessageToDBJob;
import com.fenqile.delayqueue.job.job.ProduceMessagePushJob;
import com.fenqile.delayqueue.redis.bean.DelayQueueMessage;
import com.fenqile.delayqueue.redis.config.RedisConfig;
import com.fenqile.delayqueue.redis.utils.DelayQueueRedisUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/*.xml"})
@PropertySources(value = {@PropertySource("classpath:*.properties")})
public class JobTest extends AbstractTestNGSpringContextTests {

    @Autowired
    ProduceMessagePushJob produceMessagePushJob;

    @Autowired
    MoveDelayQueueErrorMessageToDBJob moveDelayQueueErrorMessageToDBJob;

    /**
     * 测试dolphin job  消息推送
     */
    @Test
    public void testJob() {
        try {
            for (int shardItem = 1; shardItem <= ShardConfig.shardNum; shardItem++) {
                final int s = shardItem;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            produceMessagePushJob.handleJavaJob("", s, "", null);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            for (int shardItem = 1; shardItem <= 1; shardItem++) {
                final int s = shardItem;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            moveDelayQueueErrorMessageToDBJob.handleJavaJob("", s, "", null);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (Exception e) {

        }
        try {
            Thread.sleep(10000000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJob1(){

    }

    @Test
    public void initBuckets() {
        for (int shardItem = 1; shardItem <= 32; shardItem++) {
            DelayQueueMessage delayQueueMessage = new DelayQueueMessage();
            delayQueueMessage.setBody("init message !");
            delayQueueMessage.setExcuteTime(1563454692000L);

            List<String> keys = new ArrayList<>();
            List<String> args = new ArrayList<>();

            keys.add(RedisConfig.DELAY_JOB_POOL_PREFIX + shardItem);
            keys.add(RedisConfig.BUCKET_NAME_PREFIX + shardItem);
            keys.add(shardItem + "");

            args.add(JSONObject.toJSONString(delayQueueMessage));
            args.add(String.valueOf(delayQueueMessage.getExcuteTime()));

            Long result = (Long) DelayQueueRedisUtils.addJobToBucket(keys, args);
            System.out.println(result);
//
//            String pop = RedisUtils.rpop("consume_message_workqueue");
//            System.out.println(pop);
        }
    }
}
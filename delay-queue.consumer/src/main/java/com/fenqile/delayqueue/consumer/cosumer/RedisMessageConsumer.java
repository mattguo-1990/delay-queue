package com.fenqile.delayqueue.consumer.cosumer;

import com.fenqile.delayqueue.config.MessageConfig;
import com.fenqile.delayqueue.consumer.config.RedisConfig;
import com.fenqile.delayqueue.consumer.listener.RedisMessageListener;
import com.fenqile.delayqueue.redis.utils.DelayQueueRedisUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.fenqile.delayqueue.redis.config.RedisConfig.WORK_QUEUE_NAME;

/**
 * @author mattguo
 * @version 2018年07月12日 16:02
 */

@Slf4j
@Component
public class RedisMessageConsumer implements ApplicationContextAware {

    // Spring应用上下文环境
    private static ApplicationContext applicationContext;

    private static ConcurrentHashMap<String, RedisMessageListener> listeners = new ConcurrentHashMap<>();

    /**
     * 实现ApplicationContextAware接口的回调方法。设置上下文环境
     *
     * @param applicationContext
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        applicationContext = applicationContext;
    }

    public void addListener(String topic, RedisMessageListener redisMessageListener) {
        listeners.put(topic, redisMessageListener);
        new Thread(new ConsumerRunable(topic)).start();
    }

    public RedisMessageListener getListener(String topic) {
        return listeners.get(topic);
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(RedisConfig.REDIS_MESSAGE_CONSUMER_SLEEP_TIME);
        } catch (InterruptedException e) {
            log.error("{}", e);
        }
    }

    class ConsumerRunable implements Runnable {

        private String topic;

        public ConsumerRunable(String topic) {
            this.topic = topic;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    List<String> list = Lists.newArrayList(topic, WORK_QUEUE_NAME);
                    String popMessage = (String) DelayQueueRedisUtils.rpoplpush(list);
                    if (StringUtils.isNotEmpty(popMessage) && popMessage.contains(MessageConfig.SEPRATOR)) {
                        log.debug("pop message :{}", popMessage);
                        RedisMessageListener listener = listeners.get(topic);
                        listener.onEvent(popMessage);
                    }
                } catch (Exception e) {
                    log.error("RedisMessageConsumer init error ! ", e);
                }

                sleep();
            }
        }
    }
}

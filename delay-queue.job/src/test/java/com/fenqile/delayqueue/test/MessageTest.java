package com.fenqile.delayqueue.test;

import com.fenqile.delayqueue.redis.bean.Message;
import com.fenqile.delayqueue.service.DelayQueueMessageService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * @author mattguo
 * @version 2018年07月13日 18:06
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/*.xml"})
@PropertySources(value = {@PropertySource("classpath:*.properties")})
public class MessageTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private DelayQueueMessageService delayQueueMessageService;

    @Test
    public void testAddMessage() throws InterruptedException {
        //String messageId = delayQueueMessageService.addDelayMessage("testredis2", "test message 111 ", 1531363082000L);

        Message message = new Message();
        message.setBizType("1");
        message.setBody("{testtesttesttest}");
        String messageId1 = delayQueueMessageService.addDelayMessage("test-20180917", message, new Date().getTime() + 60000);
        logger.info("messageId :" + messageId1);
    }
}

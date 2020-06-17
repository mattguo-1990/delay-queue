package com.fenqile.delayqueue.test;

import com.fenqile.delayqueue.redis.utils.DelayQueueRedisUtils;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

/**
 * @author mattguo
 * @version 2018年07月09日 18:50
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/*.xml"})
@PropertySources(value = {@PropertySource("classpath:*.properties")})
public class RedisTest extends AbstractTestNGSpringContextTests {

    @Test
    public void testJob() {
        //RedisUtils.set("test20180709","hello world");
//        System.out.println("redis get key:" + RedisUtils.get("test20180709"));
//        assert null !=  RedisUtils.get("test20180709");
        //RedisUtils.del("test20180709");
        //JedisProxy.getMasterInstance().lpush()
    }

    @Test
    public void testPoP() {
//        for(int i=0;i<=100;i++){
//            Long a =RedisUtils.lpush("test_queue_test",String.valueOf(i));
//            System.out.println("a============" + a);
//        }

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String pop = DelayQueueRedisUtils.rpop("topic1");
                    System.out.println(Thread.currentThread().getName() + " pop " + pop);
                }
            }).start();
        }
    }

    @Test
    public void testPrint(){
        System.out.println("--------test--------------");
    }
}

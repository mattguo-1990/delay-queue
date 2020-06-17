package com.fenqile.delayqueue.test;

/**
 * @author mattguo
 * @version 2018年07月23日 11:34
 */

import com.fenqile.delayqueue.dao.db.dao.DelayMessageErrorDao;
import com.fenqile.delayqueue.dao.db.domain.DelayMessageError;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

/**
 * @author mattguo
 * @version 2018年07月13日 18:06
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/*.xml"})
@PropertySources(value = {@PropertySource("classpath:*.properties")})
public class DaoTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private DelayMessageErrorDao delayMessageErrorDao;

    @Test
    public void testAddErrorMessage() throws InterruptedException {
        DelayMessageError delayQueueErrorMessage = new DelayMessageError();
        delayQueueErrorMessage.setExcuteTimes(3);
        delayQueueErrorMessage.setExcuteState("FAIL");
        delayQueueErrorMessage.setErrorMsg("invoke error !!!");
        delayMessageErrorDao.insertSelective(delayQueueErrorMessage);
    }

    @Test
    public void testUpdate(){
        DelayMessageError delayQueueErrorMessage = new DelayMessageError();
        delayQueueErrorMessage.setMsgId("20180918145725814990999982944000");
        delayQueueErrorMessage.setErrorMsg("test成功");
        delayMessageErrorDao.updateByMsgId(delayQueueErrorMessage);
    }
}

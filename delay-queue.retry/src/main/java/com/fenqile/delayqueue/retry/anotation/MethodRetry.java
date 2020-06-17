package com.fenqile.delayqueue.retry.anotation;

import java.lang.annotation.*;

/**
 * @author mattguo
 * @version 2018年07月15日 20:18
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MethodRetry {
    /**
     * 重试次数 默认3次
     * @return
     */
    String retryTimes() default "3";

    /**
     * 重试间隔时间(ms) 默认3s
     * @return
     */
    String interval() default "3000";

    /**
     * 重试类型 true 异步 false 同步
     * @return
     */
    boolean isAsyn() default false;


}

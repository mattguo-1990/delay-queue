package com.fenqile.delayqueue.retry.aspect;

import com.fenqile.delayqueue.strategy.RetryStrategy;
import com.fenqile.delayqueue.retry.Bean.ClazzInfo;
import com.fenqile.delayqueue.retry.anotation.MethodRetry;
import com.fenqile.delayqueue.retry.utils.ByteArrayUtils;
import com.fenqile.delayqueue.service.DelayQueueMessageService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * @author mattguo
 * @version 2018年07月15日 20:51
 */

@Slf4j
@Aspect
@Component
public class RetryAspect {

    @Autowired
    private DelayQueueMessageService delayQueueMessageService;

    /**
     * 对指定注解，进行横切，创建一个横切的对象方法
     */
    @Pointcut("@annotation(com.fenqile.delayqueue.retry.anotation.MethodRetry)")
    public void annotationPoint() {
    }

    //环绕通知。注意要有ProceedingJoinPoint参数传入。
    @Around("annotationPoint()")
    public void retry(ProceedingJoinPoint pjp) throws Throwable {
        log.info("---------------begin-------------");

        Signature sig = pjp.getSignature();
        MethodSignature msig = null;
        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        msig = (MethodSignature) sig;
        Object target = pjp.getTarget();
        String className = target.getClass().getName();
        log.info("className -----------------------", className);

        Method method = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
        MethodRetry methodRetry = method.getAnnotation(MethodRetry.class);
        boolean isAsyn = methodRetry.isAsyn();

        if (isAsyn) {
            try {
                pjp.proceed();//执行方法;
            } catch (Throwable throwable) {
                log.error(throwable.toString());

                Class<?>[] parameterTypes = method.getParameterTypes();
                Object[] args = pjp.getArgs();

                ClazzInfo classInfo = new ClazzInfo();
                classInfo.setArgs(args);
                classInfo.setParamTypes(parameterTypes);
                classInfo.setClassName(className);
                classInfo.setMethodName(method.getName());

                byte[] bytes = ByteArrayUtils.getBytesFromObject(classInfo);
                String base64Str = Base64Utils.encodeToString(bytes);

                RetryStrategy retryStrategy = new RetryStrategy();
                retryStrategy.setRetryTimes(Integer.parseInt(methodRetry.retryTimes()));
                retryStrategy.setInterval(Long.parseLong(methodRetry.interval()));

                //TODO 208-08-20 切换为多topic之后，这里需要修改，由于时间关系，后续优化
                //delayQueueMessageService.addRetryMessage(base64Str, retryStrategy);
            }
        } else {
            doWithSpringRetry(pjp, methodRetry);
        }

        log.info("---------------end-------------");
    }

    private void doWithSpringRetry(final ProceedingJoinPoint pjp, final MethodRetry methodRetry) {
        Integer retryTimes = Integer.parseInt(methodRetry.retryTimes());
        Long interval = Long.parseLong(methodRetry.interval());

        // 构建重试模板实例
        RetryTemplate retryTemplate = new RetryTemplate();
        // 设置重试策略，主要设置重试次数
        SimpleRetryPolicy policy = new SimpleRetryPolicy(retryTimes, Collections.<Class<? extends Throwable>, Boolean>singletonMap(Exception.class, true));
        // 设置重试回退操作策略，主要设置重试间隔时间
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(interval);
        retryTemplate.setRetryPolicy(policy);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        // 通过RetryCallback 重试回调实例包装正常逻辑逻辑，第一次执行和重试执行执行的都是这段逻辑
        final RetryCallback<Object, Exception> retryCallback = new RetryCallback<Object, Exception>() {
            //RetryContext 重试操作上下文约定，统一spring-try包装
            public Object doWithRetry(RetryContext context) throws Exception {
                try {
                    pjp.proceed();//执行方法;
                } catch (Throwable throwable) {
                    log.error(throwable.toString());
                    throw new RuntimeException();//这个点特别注意，重试的根源通过Exception返回
                }
                return true;
            }
        };

        // 通过RecoveryCallback 重试流程正常结束或者达到重试上限后的退出恢复操作实例
        final RecoveryCallback<Object> recoveryCallback = new RecoveryCallback<Object>() {
            public Object recover(RetryContext context) throws Exception {

//                Signature sig = pjp.getSignature();
//                MethodSignature msig = null;
//                if (!(sig instanceof MethodSignature)) {
//                    throw new IllegalArgumentException("该注解只能用于方法");
//                }
//                msig = (MethodSignature) sig;
//                Object target = pjp.getTarget();
//                Method method = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
//
//                String serviceName = sig.getName();
//                String methodName = method.getName();
//                String params = "";
//                String appName = "";
//                String excuteTimes = methodRetry.retryTimes();
//                String excuteState = "FAIL";
//
//                RetryErrorRecord retryErrorRecord = new RetryErrorRecord();
//                retryErrorRecord.setServiceName(serviceName);
//                retryErrorRecord.setMethodName(methodName);
//                retryErrorRecord.setParams(params);
//                retryErrorRecord.setAppName(appName);
//                retryErrorRecord.setExcuteTimes(excuteTimes);
//                retryErrorRecord.setExcuteState(excuteState);
//
//                delayQueueMessageService.addRetryErrorRecord(retryErrorRecord);

                //写入数据库
                return null;
            }
        };
        try {
            retryTemplate.execute(retryCallback, recoveryCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
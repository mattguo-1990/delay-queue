package com.fenqile.delayqueue.redis.utils;

import com.fenqile.delayqueue.redis.bean.DelayQueueJob;
import com.fenqile.redis.JedisProxy;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Tuple;

import java.util.List;
import java.util.Set;

/**
 * @author guoji
 * @version 20180528
 */
@Slf4j
public class DelayQueueRedisUtils {

    private static String REDIS_KEY = "Payment_Dq_Cache";

    /**
     * 将job添加到bucket中
     *
     * @param bucketName
     * @param score
     * @param jobId
     */
    public static void addJob(String bucketName, String jobId, double score) {
        JedisProxy.getMasterCache(REDIS_KEY).zadd(bucketName, score, jobId);
    }

    /**
     * 新增job到bucket，sorted set增加item
     * KEYS[1] redis keyName
     * KEYS[2] bucketName
     * KEYS[3] jobId
     * <p>
     * ARGV[1] message
     * ARGV[2] message executeTime
     *
     * @param keys
     * @param args
     * @return
     */
    public static Object addJobToBucket(List<String> keys, List<String> args) {
        String script =
                "local meta = redis.call('GET', KEYS[1]) " +
                        "if not meta then redis.call('SET', KEYS[1], ARGV[1]) " +
                        "redis.call('ZADD', KEYS[2], ARGV[2], KEYS[3]) " +
                        "return 1 else return 0 end";
        return JedisProxy.getMasterCache(REDIS_KEY).eval(script, keys, args);
    }

    /**
     * 从工作队列取出item并把item添加到sorted set
     * KEYS[1] redis workQueueName
     * KEYS[2] bucketName
     * <p>
     * ARGV[2] message executeTime
     *
     * @param keys
     * @param args
     * @return
     */
    public static Object rpopFromListAndaddJobToBucket(List<String> keys, List<String> args) {
        String script =
                "local meta = redis.call('RPOP', KEYS[1]) " +
                        "if meta then " +
                        "redis.call('ZADD', KEYS[2], ARGV[1], meta) " +
                        "return 1 else return 0 end";
        return JedisProxy.getMasterCache(REDIS_KEY).eval(script, keys, args);
    }

    /**
     * 推送jobId至Redis List
     * KEYS[1] redis List Name
     * KEYS[2] bucketName
     * <p>
     * ARGV[1] jobId
     * ARGV[1] jobId
     *
     * @param keys
     * @param args
     * @return
     */
    public static Object pushJobToRedisList(List<String> keys, List<String> args) {
        String script =
                "local meta = redis.call('LPUSH', KEYS[1], ARGV[1])  " +
                        "if meta > 0 then " +
                        "redis.call('ZREM', KEYS[2], ARGV[2]) " +
                        "return 1 else return 0 end ";
        return JedisProxy.getMasterCache(REDIS_KEY).eval(script, keys, args);
    }

    /**
     * KEYS[1] jobId
     * KEYS[2] workQueueName
     * KEYS[3] jobId,excuteTime
     *
     * @param keys
     * @param args
     * @return
     */
    public static Object delJob(List<String> keys, List<String> args) {
        String script =
                "local meta = redis.call('DEL', KEYS[1]) " +
                        "if meta > 0 then " +
                        "redis.call('LREM', KEYS[2], 0, KEYS[3]) " +
                        "return 1 else return 0 end ";
        return JedisProxy.getMasterCache(REDIS_KEY).eval(script, keys, args);
    }

    /**
     * 从延迟任务桶中获取score最小的 jodId
     *
     * @param bucketName
     * @return
     */
    public static DelayQueueJob getFromBucket(String bucketName) {
        Set<Tuple> set = JedisProxy.getMasterCache(REDIS_KEY).zrangeWithScores(bucketName, 0, 0);
        if (set != null && !set.isEmpty()) {
            Tuple tuple = set.iterator().next();
            DelayQueueJob delayQueueJob = new DelayQueueJob();
            delayQueueJob.setJobId(tuple.getElement());
            delayQueueJob.setScore(tuple.getScore());
            return delayQueueJob;
        }
        return null;
    }

    /**
     * 从延迟任务桶中查询下标参数start 和end之间的成员列表
     *
     * @param bucketName
     * @param startNum
     * @param endNum
     * @return
     */
    public static Set<Tuple> zrangeWithScores(String bucketName, long startNum, long endNum) {
        Set<Tuple> set = JedisProxy.getMasterCache(REDIS_KEY).zrangeWithScores(bucketName, startNum, endNum);
        return set;
    }

    /**
     * 根据key获取value
     *
     * @param key
     * @return
     */
    public static String getValue(String key) {
        return JedisProxy.getMasterCache(REDIS_KEY).get(key);
    }

    /**
     * 从延迟任务桶中删除 jodId
     *
     * @param bucketName
     * @param jobId
     */
    public static Long removeFromBucket(String bucketName, String jobId) {
        return JedisProxy.getMasterCache(REDIS_KEY).zrem(bucketName, jobId);
    }

    /**
     * 从工作队列 workQueue 中删除 jodId
     *
     * @param queueName
     * @param jobId
     */
    public static Long removeFromWorkQueue(String queueName, String jobId) {
        return JedisProxy.getMasterCache(REDIS_KEY).lrem(queueName, 0, jobId);
    }

    /**
     * 如果field 是哈希表中的一个新建域，并且值设置成功，返回 1 。如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0 。
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public static Long put(final String key, final String field, final String value) {
        return JedisProxy.getMasterCache(REDIS_KEY).hset(key, field, value);
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public static String set(final String key, final String value) {
        return JedisProxy.getMasterCache(REDIS_KEY).set(key, value);
    }

    /**
     * @param key
     * @param field
     * @return
     */
    public static String get(final String key, final String field) {
        return JedisProxy.getMasterCache(REDIS_KEY).hget(key, field);
    }

    /**
     * 获取元信息池中的所有field
     *
     * @param key
     * @return
     */
    public static Set<String> getAllFields(final String key) {
        return JedisProxy.getMasterCache(REDIS_KEY).hkeys(key);
    }

    /**
     * 删除返回1，其他返回0
     *
     * @param key
     * @param field
     */
    public static Long hdel(final String key, final String field) {
        return JedisProxy.getMasterCache(REDIS_KEY).hdel(key, field);
    }

    /**
     * 删除返回1，其他返回0
     *
     * @param key
     */
    public static Long del(final String key) {
        return JedisProxy.getMasterCache(REDIS_KEY).del(key);
    }

    /**
     * @param key
     * @return 返回影响的条数
     */
    public static Long lpush(final String key, final String... value) {
        return JedisProxy.getMasterInstance().lpush(key, value);
    }

    /**
     * @param key
     * @return 返回获取的元素
     */
    public static String rpop(final String key) {
        return JedisProxy.getMasterInstance().rpop(key);
    }

    /**
     * @param keys
     * @return 返回是否成功
     */
    public static Object rpoplpush(List<String> keys) {
        String script = "local meta = redis.call('RPOPLPUSH', KEYS[1], KEYS[2]) " +
                "if meta then " +
                "return meta else return nil end ";
        final List<String> args = Lists.newArrayList();
        return JedisProxy.getMasterCache(REDIS_KEY).eval(script, keys, args);
    }

    /**
     * @param key
     * @return 返回对应范围内的数据
     */
    public static List<String> lrange(final String key, final Integer start, Integer end) {
        return JedisProxy.getMasterInstance().lrange(key, start, end);
    }

    /**
     * @param key
     * @return 返回对应位置的数据
     */
    public static String lindex(final String key, final Integer index) {
        return JedisProxy.getMasterInstance().lindex(key, index);
    }
}
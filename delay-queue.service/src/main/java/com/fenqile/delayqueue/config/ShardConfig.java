package com.fenqile.delayqueue.config;

/**
 * @author mattguo
 * @version 2018年07月10日 15:54
 */
public class ShardConfig {

    /**
     * 槽数量配置
     */
    public static final Integer shardNum = 16;

    /**
     * 通过时分秒索引截取时分秒(起始位置)
     */
    public static final Integer timeSubStart = 8;

    /**
     * 通过时分秒索引截取时分秒(结束位置)
     */
    public static final Integer timeSubEnd = 14;
}

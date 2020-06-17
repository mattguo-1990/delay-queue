/**
 * 描述
 *
 * @name Main.java
 * @copyright Copyright by fenqile.com
 * @author weikuo
 * @version 2015年12月21日
 **/
package com.fenqile.delayqueue.job;

/**
 * 入口程序
 *
 * @author paty
 * @version 2015年12月21日
 * @see com.fenqile.delayqueue.job.Main
 */

public class Main {

    static {
        // 设置dubbo使用slf4j来记录日志
        System.setProperty("dubbo.application.logger", "slf4j");
    }

    /**
     * 主函数
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        com.alibaba.dubbo.container.Main.main(args);
    }

}

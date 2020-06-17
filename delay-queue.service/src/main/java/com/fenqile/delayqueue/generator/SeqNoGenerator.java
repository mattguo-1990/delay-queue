package com.fenqile.delayqueue.generator;

import com.alibaba.dubbo.common.utils.NetInterfaceUtils;
import com.fenqile.delayqueue.config.ShardConfig;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mattguo
 * @version 2018年07月10日 15:30
 */
public class SeqNoGenerator {
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    private static final int AUTOINCREMENT_MIN = 0;

    private static final int AUTOINCREMENT_MAX = 9999;

    private static final String DEFAULT_IP_HEX = "000000000000";

    private static AtomicInteger GENERATOR = new AtomicInteger(AUTOINCREMENT_MIN);

    public static String genId() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String dsteStr = sdf.format(date);
        //每天的毫秒数
        long l = 1000;
        long timeMillis = date.getTime() % l;

        StringBuilder serialNum = new StringBuilder();
        serialNum.append(dsteStr);
        serialNum.append(String.format("%03d", timeMillis));
        serialNum.append(getIpValue());
        //serialNum.append(String.format("%04d", getProcessID()));
        serialNum.append(String.format("%03d", autoIncrement()));
        //int uidInt = uid % UID_MODULO_BASE;
        //serialNum.append(String.format("%03d", uidInt));
        return serialNum.toString().toUpperCase();
    }

    /**
     * 获取进程id
     * @return
     */
    public static int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Integer.parseInt(runtimeMXBean.getName().split("@")[0])%10000;
    }
    /**
     * 获取ip数值
     * 混淆规则 1000 - 各段ip值
     * @return
     */
    public static String getIpValue(){
        String ip = NetInterfaceUtils.getIp();
        String ipValue = null;
        if(ip.contains(".")){
            String[] ipaddr = ip.split("\\.");
            if(ipaddr.length == 4){
                StringBuilder str = new StringBuilder();
                for(int i = 0; i < ipaddr.length; i++){
                    Integer ipaddrInt = 1000 - Integer.parseInt(ipaddr[i]);
                    str.append(ipaddrInt);
                }
                ipValue = str.toString();
            } else {
                ipValue = DEFAULT_IP_HEX;
            }
        } else {
            ipValue = DEFAULT_IP_HEX;
        }
        return ipValue;
    }

    private static int autoIncrement() {
        return incrementAndGet(GENERATOR, AUTOINCREMENT_MAX, AUTOINCREMENT_MIN);
    }

    /**
     * 重写了incrementAndGet，增加到达边界值后清零的动作
     *
     * @param atomicInteger
     * @param upperBound
     * @param lowerBound
     * @return
     */
    private static int incrementAndGet(AtomicInteger atomicInteger, int upperBound, int lowerBound) {
        for (; ; ) {
            int current = atomicInteger.get();
            if (current == upperBound) {
                //如果当前值已经到了最大值，则和最大值CAS
                if (atomicInteger.compareAndSet(upperBound, lowerBound)) {
                    return lowerBound;
                } else {
                    continue;
                }
            }
            int next = current + 1;
            if (atomicInteger.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    public static void main(String[] args){
//        for(int i= 0;i<100;i++){
//            String id = genId();
//            id = id.substring(8,14);
//            Integer timeInteger = Integer.parseInt(id);
//            //System.out.println(timeInteger);
//            System.out.println(timeInteger % ShardConfig.shardNum);
//            try {
//                Thread.sleep(1000L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        System.out.println(genId());
    }
}

package com.fenqile.delayqueue.job.util;

import java.util.List;

/**
 * @author mattguo
 * @version 2018年08月07日 20:21
 */
public class CollectionUtil {

    public static <T> T getFirstElement(List<T> list){
        if(list == null || list.size() == 0){
            return null;
        }
        return list.get(0);
    }
}

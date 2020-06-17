package com.fenqile.delayqueue.retry.Bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author mattguo
 * @version 2018年07月16日 20:47
 */
@Data
public class ClazzInfo implements Serializable {
    private String className;
    private String methodName;
    private Class<?>[] paramTypes;
    private Object[] args;
}

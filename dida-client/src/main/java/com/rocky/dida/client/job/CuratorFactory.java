package com.rocky.dida.client.job;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created by rocky on 18/2/28.
 */
public class CuratorFactory {
    private final static int baseSleepTimeMs = 1000; //基础睡眠时间, mills
    private final static int maxRetries = 10; //重试次数

    public static CuratorFramework create(String connectString){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries);
        return CuratorFrameworkFactory.newClient(connectString, retryPolicy);

    }
}

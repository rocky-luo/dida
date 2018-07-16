package com.rocky.dida.client.job;

import com.rocky.dida.common.rpc.AdminService;

/**
 * Created by rocky on 18/2/22.
 */
public class SchedulerFactory {
    private static volatile IScheduler scheduler;
    public static IScheduler get(AdminService.Iface adminService) {
        if (scheduler == null) {
            synchronized (SchedulerFactory.class) {
                if (scheduler == null) {
                    scheduler = new SchedulerImpl(adminService);
                }
            }
        }
        return scheduler;
    }
}

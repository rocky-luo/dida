package com.rocky.dida.client.job;

import org.quartz.*;

import java.util.Date;

/**
 * Created by rocky on 18/2/23.
 */
public class TriggerApply implements Job{
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Date scheduleFireDate = context.getScheduledFireTime();
        Date actualFireDate = context.getFireTime();
        TriggerKey triggerKey = context.getTrigger().getKey();
        String group = triggerKey.getGroup();
        String name = triggerKey.getName();
        String cron = ((CronTrigger) context.getTrigger()).getCronExpression();

        // TODO: 18/2/23 提交任务到调度中心


    }
}

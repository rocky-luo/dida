package com.rocky.dida.client.job;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rocky.dida.common.rpc.AdminService;
import org.quartz.*;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by rocky on 18/2/22.
 */
public class SchedulerImpl implements IScheduler {
    private final static Logger LOGGER = LoggerFactory.getLogger(SchedulerImpl.class);
    private Scheduler scheduler = null;
    private Map<String, JobContext> jobMap = Maps.newConcurrentMap();
    private AdminService.Iface adminService = null;

    public SchedulerImpl(AdminService.Iface adminService) {
        this.adminService = adminService;
        SchedulerFactory sf = new StdSchedulerFactory();
        try {
            //生成Scheduler
            this.scheduler = sf.getScheduler();
            //开始Scheduler
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException("生成Scheduler失败!", e);
        }
    }

    @Override
    public void add(JobContext jobContext) {
        try {
            if (checkJobExist(jobContext.getGroup(), jobContext.getName())) {
                // 如果存在job,则采用update
                update(jobContext);
            } else {
                JobDetail jobDetail = JobBuilder.newJob(TriggerReport.class)
                        .withIdentity(jobContext.getName(), jobContext.getGroup())
                        .build();
                // 任务触发相关参数
                jobDetail.getJobDataMap().put(TriggerReport.JOB_DATA_KEY_ADMIN_SERVICE, this.adminService);
                CronTrigger cronTrigger = cronTrigger(jobContext.getGroup(), jobContext.getName(), jobContext.getCron());
                this.scheduler.scheduleJob(jobDetail, cronTrigger);
                jobMap.put(jobKey(jobContext.getGroup(), jobContext.getName()), jobContext);

            }
        }catch (SchedulerException e) {
            throw new RuntimeException(String.format("添加job失败,group=%s, name=%s", jobContext.getGroup(), jobContext.getName()), e);
        }
    }

    @Override
    public void update(JobContext jobContext) {
        try {
            Date firstTrigger = scheduler.rescheduleJob(TriggerKey.triggerKey(jobContext.getName(), jobContext.getGroup()), cronTrigger(jobContext.getGroup(), jobContext.getName(), jobContext.getCron()));
            if (firstTrigger == null) {
                throw new RuntimeException(String.format("无法找到对应的CronTrigger,group=%s, name=%s", jobContext.getGroup(),jobContext.getName()));
            }
            jobMap.put(jobKey(jobContext.getGroup(), jobContext.getName()), jobContext);
        } catch (SchedulerException e) {
            throw new RuntimeException(String.format("更新任务发生错误,group=%s, name=%s", jobContext.getGroup(),jobContext.getName()), e);
        }
    }

    @Override
    public void delete(String group, String name) {
        try {
            boolean result = this.scheduler.unscheduleJob(TriggerKey.triggerKey(name, group));
            if (result) {
                jobMap.remove(jobKey(group, name));
            }else {
                throw new RuntimeException(String.format("删除任务失败, group=%s, name=%s", group, name));
            }

        } catch (SchedulerException e) {
            throw new RuntimeException(String.format("删除任务失败, group=%s, name=%s", group, name), e);
        }
    }

    @Override
    public List<JobContext> getAllJobs() {
        return Lists.newArrayList(jobMap.values());
    }

    @Override
    public String executeJob(String group, String name, String param) throws Exception{
        JobContext thisJobCon = this.jobMap.get(jobKey(group, name));
        String exeResult = thisJobCon.getJob().execute(param);
        return exeResult;
    }

    boolean checkJobExist(String group, String name) throws SchedulerException {
        return this.scheduler.checkExists(TriggerKey.triggerKey(name, group));
    }

    CronTrigger cronTrigger(String group, String name, String cron) {
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity(name, group)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                // TODO: 18/2/22 是否应该现在start?
                .startNow()
                .build();
        return cronTrigger;
    }

    private String jobKey(String group, String name) {
        return group + ":" + name;
    }
}

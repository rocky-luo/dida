package com.rocky.dida.client.job;

import com.alibaba.fastjson.JSON;
import com.rocky.dida.common.rpc.AdminService;
import com.rocky.dida.common.rpc.ApplyExecuteJobForm;
import org.apache.thrift.TException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by rocky on 18/2/28.
 */
public class TriggerReport implements Job{
    private final static Logger LOGGER = LoggerFactory.getLogger(TriggerReport.class);
    public static final String JOB_DATA_KEY_ADMIN_SERVICE = "adminService";
    public static final String JOB_DATA_KEY_JOB_CONTEXT = "jobContext";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        AdminService.Iface adminService = (AdminService.Iface) context.getMergedJobDataMap().get(JOB_DATA_KEY_ADMIN_SERVICE);
        JobContext jobContext = (JobContext) context.getMergedJobDataMap().get(JOB_DATA_KEY_JOB_CONTEXT);
        ApplyExecuteJobForm applyForm = new ApplyExecuteJobForm();
        applyForm.setGroup(jobContext.getGroup());
        applyForm.setName(jobContext.getName());
        applyForm.setCron(jobContext.getCron());
        applyForm.setScheduleTime(context.getScheduledFireTime().getTime());
        applyForm.setTriggerTime(context.getScheduledFireTime().getTime());
        // TODO: 18/3/1 申请机器id
        applyForm.setApplicantId(null);
        try {
            adminService.applyExecuteJob(applyForm);
        } catch (TException e) {
            // TODO: 18/3/1 重试
            String errorMsg = String.format("thrift error, apply form:%s", JSON.toJSONString(applyForm));
            throw new JobExecutionException(errorMsg, e);
        }

    }
}

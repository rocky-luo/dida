package com.rocky.dida.client.job;

import com.rocky.dida.common.rpc.ClientService;
import com.rocky.dida.common.rpc.DidaJob;
import com.rocky.dida.common.rpc.ExecuteResult;
import com.rocky.dida.common.rpc.JobExecuteException;
import org.apache.thrift.TException;

/**
 * Created by rocky on 18/3/8.
 */
public class ClientServiceImpl implements ClientService.Iface {
    private IScheduler scheduler;


    @Override
    public ExecuteResult executeJob(String group, String name, String param, String jobInstanceId) throws JobExecuteException, TException {
        ExecuteResult executeResult = new ExecuteResult();
        String stringRes = null;
        executeResult.setActualStartTime(System.currentTimeMillis());
        try {
            stringRes = this.scheduler.executeJob(group, name, param);
        } catch (Exception e) {
            JobExecuteException jee = new JobExecuteException();
            jee.setErrorMsg(e.getMessage());
            String strStackTrace = "";
            for (StackTraceElement stackTraceElement : e.getStackTrace()){
                strStackTrace += stackTraceElement.toString();
            }
            executeResult.setCode(-1);
            jee.setStackTraceString(strStackTrace);
            throw jee;
        } finally {
            executeResult.setActualEndTime(System.currentTimeMillis());
            executeResult.setJobInstanceId(jobInstanceId);
            executeResult.setReturnString(stringRes);
        }
        return executeResult;
    }

    @Override
    public void updateCron(DidaJob job) throws TException {

    }

    @Override
    public void deleteJob(String group, String name) throws TException {

    }
}

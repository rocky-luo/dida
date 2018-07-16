namespace java com.rocky.dida.common.rpc

struct DidaJob {
    1:string group;
    2:string name;
    3:string cron;
}

struct ApplyExecuteJobForm {
    1:string group;
    2:string name;
    3:string cron;
    4:i64 scheduleTime;
    5:i64 triggerTime;
    6:string applicantId;
}

struct ExecuteResult {
    1:string jobInstanceId;
    2:i32 code;
    3:i64 actualStartTime;
    4:i64 actualEndTime;
    5:string returnString;
}

exception JobExecuteException {
    1:string errorMsg;
    2:string stackTraceString;
}

service ClientService {
    ExecuteResult executeJob(1:string group, 2:string name, 3:string param, 4:string jobInstanceId) throws (1:JobExecuteException jee);

    void updateCron(1:DidaJob job);

    void deleteJob(1:string group, 2:string name);
}

service AdminService {
    list<DidaJob> crons(1:list<DidaJob> jobs);

    i32 applyExecuteJob(1:ApplyExecuteJobForm form);


}


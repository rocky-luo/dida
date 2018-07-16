package com.rocky.dida.client.job;

import java.util.List;

/**
 * Created by rocky on 18/2/22.
 */
public interface IScheduler {
    void add(JobContext jobContext);
    void update(JobContext jobContext);
    void delete(String group, String name);
    List<JobContext> getAllJobs();

    String executeJob(String group, String name, String param) throws Exception;
}

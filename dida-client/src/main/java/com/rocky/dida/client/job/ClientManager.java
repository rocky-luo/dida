package com.rocky.dida.client.job;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.rocky.dida.common.rpc.DidaJob;
import com.rocky.dida.registry.Registry;
import com.rocky.dida.registry.ZkRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.thrift.TException;

import java.util.List;

/**
 * Created by rocky on 18/2/23.
 */
public class ClientManager {
    private ClientConfig clientConfig;
    private IScheduler scheduler;
    private List<JobContext> jobContexts;
    private Registry registry;

    private CuratorFramework curatorFramework;
    private AdminRpcImpl adminRpc;


    public void start() {
        this.curatorFramework = CuratorFactory.create(this.clientConfig.getRegistryConnect());
        this.registry = new ZkRegistry(this.curatorFramework);
        this.adminRpc = new AdminRpcImpl(this.registry);
        List<DidaJob> queryJobs = Lists.transform(this.jobContexts, new Function<JobContext, DidaJob>() {
            @Override
            public DidaJob apply(JobContext input) {
                if (input == null) return null;
                return new DidaJob(input.getGroup(), input.getName(), null);
            }
        });
        List<DidaJob> didaJobs = null;
        try {
            didaJobs = this.adminRpc.crons(queryJobs);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
        IScheduler scheduler = SchedulerFactory.get(this.adminRpc);
        for (DidaJob didaJob : didaJobs) {
            JobContext jobContext = new JobContext();
            jobContext.setGroup(didaJob.getGroup());
            jobContext.setName(didaJob.getName());
            jobContext.setCron(didaJob.getCron());
            scheduler.add(jobContext);
        }
    }
}

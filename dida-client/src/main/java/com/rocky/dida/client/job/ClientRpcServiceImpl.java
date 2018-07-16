package com.rocky.dida.client.job;

import com.rocky.dida.common.rpc.ClientService;
import com.rocky.dida.common.rpc.DidaJob;
import com.rocky.dida.common.rpc.ExecuteResult;
import org.apache.thrift.TException;

/**
 * Created by rocky on 18/3/1.
 */
public class ClientRpcServiceImpl implements ClientService.Iface{
    @Override
    public ExecuteResult executeJob(String group, String name, String jobInstanceId) throws TException {

    }

    @Override
    public void updateCron(DidaJob job) throws TException {

    }

    @Override
    public void deleteJob(String group, String name) throws TException {

    }
}

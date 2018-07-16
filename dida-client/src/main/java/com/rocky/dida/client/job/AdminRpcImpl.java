package com.rocky.dida.client.job;

import com.rocky.dida.common.model.Node;
import com.rocky.dida.common.rpc.AdminService;
import com.rocky.dida.common.rpc.ApplyExecuteJobForm;
import com.rocky.dida.common.rpc.DidaJob;
import com.rocky.dida.registry.Registry;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.List;

/**
 * Created by rocky on 18/2/24.
 */
public class AdminRpcImpl implements AdminService.Iface{

    private AdminHolder adminHolder;

    AdminRpcImpl(Registry registry) {
        this.adminHolder = new AdminHolder(registry);
    }

    @Override
    public List<DidaJob> crons(List<DidaJob> jobs) throws TException {
        Node adminNode = this.adminHolder.randomAdminNode();
        TTransport transport = new TFramedTransport(new TSocket(adminNode.getIp(), adminNode.getPort()));
        TProtocol protocol = new TCompactProtocol(transport);
        AdminService.Iface client = new AdminService.Client(protocol);
        try {
            transport.open();
            return client.crons(jobs);
        }finally {
            if (transport != null) {
                transport.close();
            }
        }

    }

    @Override
    public int applyExecuteJob(ApplyExecuteJobForm form) throws TException {
        Node adminNode = this.adminHolder.randomAdminNode();
        TTransport transport = new TFramedTransport(new TSocket(adminNode.getIp(), adminNode.getPort()));
        TProtocol protocol = new TCompactProtocol(transport);
        AdminService.Iface client = new AdminService.Client(protocol);
        try {
            transport.open();
            return client.applyExecuteJob(form);
        }finally {
            if (transport != null) {
                transport.close();
            }
        }
    }
}

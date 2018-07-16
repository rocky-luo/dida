package com.rocky.dida.client.job;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rocky.dida.common.model.Node;
import com.rocky.dida.common.utils.CollectionUtil;
import com.rocky.dida.common.utils.RandomUtil;
import com.rocky.dida.registry.NotifyEvent;
import com.rocky.dida.registry.NotifyListener;
import com.rocky.dida.registry.Registry;
import com.rocky.dida.registry.RegistryContent;

import java.util.List;
import java.util.Map;

/**
 * Created by rocky on 18/2/23.
 */
public class AdminHolder {
    private Registry registry;
    private Map<String, Node> adminNodeMap = Maps.newConcurrentMap();

    public AdminHolder(Registry registry) {
        this.registry = registry;
        List<String> strAdmins = registry.lookup(RegistryContent.adminUrlPrefix());
        for (String strAdmin : strAdmins) {
            Node adminNode = JSON.parseObject(strAdmin, Node.class);
            this.adminNodeMap.put(adminNode.getId(), adminNode);
        }

        this.registry.subscribe(RegistryContent.adminUrlPrefix(), new NotifyListener() {
            @Override
            public void onChanged(NotifyEvent notifyEvent) {
                synchronized (AdminHolder.class) {
                    if (NotifyEvent.Event.ADD_NODE == notifyEvent.getEvent()
                            || NotifyEvent.Event.MODIFY_NODE == notifyEvent.getEvent()) {
                        //增加admin节点
                        Node node = JSON.parseObject(notifyEvent.getData(), Node.class);
                        adminNodeMap.put(node.getId(), node);
                    } else if (NotifyEvent.Event.DELETE_NODE == notifyEvent.getEvent()) {
                        // TODO: 18/2/24 有问题
                        adminNodeMap.remove(notifyEvent.getNodePath());
                    }
                }

            }
        });
    }


    public Node randomAdminNode() {
        synchronized (AdminHolder.class) {
            List<Node> adminNodes = Lists.newArrayList(adminNodeMap.values());
            if (CollectionUtil.isEmpty(adminNodes)) {
                return null;
            } else {
                int randomIdx = RandomUtil.intRandom(0, adminNodes.size() - 1);
                return adminNodes.get(randomIdx);
            }
        }
    }


}

package com.rocky.dida.registry;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by rocky on 17/10/16.
 */
public class ZkRegistry implements Registry {
    private final static Logger LOGGER = LoggerFactory.getLogger(ZkRegistry.class);
    private CuratorFramework curatorFramework;
    private final static int baseSleepTimeMs = 1000; //基础睡眠时间, mills
    private final static int maxRetries = 10; //重试次数
    final private Map<String, PathChildrenCache> urlCacheMap = Maps.newHashMapWithExpectedSize(10);
    private Map<String, Set<NotifyCacheListener>> urlListenersMap = Maps.newHashMapWithExpectedSize(10);
    private Map<String, PersistentNode> urlRegisterMap = Maps.newHashMapWithExpectedSize(10);

    public ZkRegistry(CuratorFramework curatorFramework) {
        this.curatorFramework  = curatorFramework;
        curatorFramework.start();
    }

    @Override
    public void register(String url, String data) {
        synchronized (this) {
            PersistentNode registryNode = this.urlRegisterMap.get(url);
            if (registryNode != null) {
                // 如果存在,说明已经注册,则修改data数据
                try {
                    registryNode.setData(bytesData(data));
                } catch (Exception e) {
                    throw new RuntimeException(String.format("modify registered node in zookeeper failed, path:{}, data:{}",url, data));
                }
            }else {
                registryNode = new PersistentNode(this.curatorFramework, CreateMode.EPHEMERAL, false, url,bytesData(data));
                registryNode.start();
                try {
                    boolean created = registryNode.waitForInitialCreate(3000, TimeUnit.MILLISECONDS);
                    if (!created) {
                        throw new RuntimeException("zookeeper create node failed, when create the node " + "[" + url + "]");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void unregister(String url) {
        synchronized (this) {
            PersistentNode registryNode = this.urlRegisterMap.get(url);
            if (registryNode != null) {
                try {
                    registryNode.close();
                    this.urlRegisterMap.remove(url);
                } catch (IOException e) {
                    throw new RuntimeException("zookeeper stop node failed, when stop the node " + "[" + url + "]");
                }
            }
        }
    }

    @Override
    public void subscribe(String url, final NotifyListener listener) {
        synchronized (this) {
            PathChildrenCache childrenCache = urlCacheMap.get(url);
            if (childrenCache == null) {
                childrenCache = new PathChildrenCache(this.curatorFramework, url,true);
                NotifyCacheListener cacheListener = new NotifyCacheListener(listener);
                childrenCache.getListenable().addListener(cacheListener);
                try {
                    childrenCache.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                urlCacheMap.put(url, childrenCache);
                urlListenersMap.put(url, Sets.newHashSet(cacheListener));
            }else {
                Set<NotifyCacheListener> notifyCacheListeners = urlListenersMap.get(url);
                boolean hasLitener = false;
                for (NotifyCacheListener ncl : notifyCacheListeners) {
                    if (ncl.getNotifyListener().equals(listener)) {
                        hasLitener = true;
                        break;
                    }
                }
                if (!hasLitener) {
                    NotifyCacheListener cacheListener = new NotifyCacheListener(listener);
                    childrenCache.getListenable().addListener(cacheListener);
                    notifyCacheListeners.add(cacheListener);
                }

            }
        }

    }

    @Override
    public void unSubscribe(String url, NotifyListener listener) {
        synchronized (this) {
            PathChildrenCache childrenCache = urlCacheMap.get(url);
            if (childrenCache != null) {
                Set<NotifyCacheListener> cacheListeners = urlListenersMap.get(url);
                for (NotifyCacheListener ncl : cacheListeners) {
                    if (ncl.getNotifyListener().equals(listener)) {
                        childrenCache.getListenable().removeListener(ncl);
                        cacheListeners.remove(ncl);
                    }
                }
            }
        }
    }

    @Override
    public List<String> lookup(String url) {
        List<String> childrenData = Lists.newArrayListWithCapacity(5);
        List<String> children = null;
        try {
            children = this.curatorFramework.getChildren().forPath(url);
            for (String child :children) {
                String childPath = url + "/" + child;
                byte[] childByte = this.curatorFramework.getData().forPath(childPath);
                String childString = new String(childByte, Charsets.UTF_8);
                childrenData.add(childString);
            }
            return childrenData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class NotifyCacheListener implements PathChildrenCacheListener {
        private NotifyListener notifyListener;

        private NotifyCacheListener() {
        }

        public NotifyCacheListener(NotifyListener notifyListener) {
            this.notifyListener = notifyListener;
        }
        @Override
        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
            ChildData childData = event.getData();
            NotifyEvent notifyEvent = new NotifyEvent();
            switch (event.getType()) {
                case CHILD_ADDED:
                    notifyEvent.setEvent(NotifyEvent.Event.ADD_NODE);
                    break;
                case CHILD_UPDATED:
                    notifyEvent.setEvent(NotifyEvent.Event.MODIFY_NODE);
                    break;
                case CHILD_REMOVED:
                    notifyEvent.setEvent(NotifyEvent.Event.DELETE_NODE);
                    break;
                default:
                    // 其他事件,比如说重连接,不需要
                    return;
            }
            notifyEvent.setNodePath(childData.getPath());
            notifyEvent.setData(new String(childData.getData()));
            this.notifyListener.onChanged(notifyEvent);
        }

        public NotifyListener getNotifyListener() {
            return this.notifyListener;
        }
    }


    private byte[] bytesData(String data){
        return data == null ? "".getBytes() : data.getBytes();
    }
}

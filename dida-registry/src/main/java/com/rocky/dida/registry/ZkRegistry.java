package com.rocky.dida.registry;



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
    private String url;
    private String data;
    private PersistentNode persistentNode;
    final private Map<String, PathChildrenCache> urlCacheMap = Maps.newHashMapWithExpectedSize(10);
    private Map<String, Set<NotifyCacheListener>> urlListenersMap = Maps.newHashMapWithExpectedSize(10);

    public ZkRegistry(String connectString, String url, String data) {
        this.url = url;
        this.data = data;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries);
        this.curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        byte[] bytesData = data == null ? "".getBytes() : data.getBytes();
        this.persistentNode = new PersistentNode(curatorFramework, CreateMode.EPHEMERAL, false, url, bytesData);
    }

    @Override
    public void register() {
        this.persistentNode.start();
        try {
            boolean created = persistentNode.waitForInitialCreate(3000, TimeUnit.MILLISECONDS);
            if (!created) {
                throw new RuntimeException("zookeeper create node failed, when create the node " + "[" + this.url + "]");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregister() {
        try {
            this.persistentNode.close();
        } catch (IOException e) {
            throw new RuntimeException("zookeeper stop node failed, when stop the node " + "[" + this.url + "]");
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
}

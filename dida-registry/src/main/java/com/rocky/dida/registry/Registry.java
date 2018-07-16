package com.rocky.dida.registry;

import java.util.List;

/**
 * Created by rocky on 17/10/13.
 */
public interface Registry {
    void register(String url, String data);
    void unregister(String url);

    void subscribe(String url, NotifyListener listener);
    void unSubscribe(String url, NotifyListener listener);

    List<String> lookup(String url);
}

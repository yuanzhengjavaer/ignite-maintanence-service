package com.nomura.service;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.events.EventType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;


@Service
public class CacheEventService implements InitializingBean {
    @Autowired
    @Lazy
    private Ignite ignite;
    private IgniteCache<Object, Object> cache;

    public void put(Integer key, String val) {
        Lock lock = cache.lock(key);
        lock.lock();
        cache.put(key, val);
    }

    /**
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //local event
        ClusterGroup clusterGroup = ignite.events().clusterGroup().forCacheNodes("cacheEvent");
        ignite.events(clusterGroup).localListen(event -> {
                    System.out.println("event = " + event);
                    //write compute logic...
                    return true;
                }, EventType.EVT_CACHE_OBJECT_PUT, EventType.EVT_CACHE_OBJECT_READ,
                EventType.EVT_CACHE_OBJECT_REMOVED);
        //remote event , clusterGroup can add more condition to find nodes that u need to listen
        ignite.events(clusterGroup).remoteListen((uuid, event) -> {
                    //write compute logic...
                    System.out.println("uuid = " + uuid);
                    return true;
                },//filter event type
                event -> {
                    System.out.println("event = " + event);
                    return true;
                }, EventType.EVT_CACHE_OBJECT_PUT, EventType.EVT_CACHE_OBJECT_READ,
                EventType.EVT_CACHE_OBJECT_REMOVED);
        cache = ignite.cache("cacheEvent");
    }
}

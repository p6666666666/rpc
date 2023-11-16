package com.czp.loadbalancer;

import com.czp.PrpcBootstrap;
import com.czp.discovery.Registry;
import com.czp.exception.LoadBalancerException;
import com.czp.loadbalancer.impl.RoundRobinLoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class AbstractLoadBalancer implements LoadBalancer{


    private Map<String,Selector> cache=new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName,String group) {
        Selector selector = cache.get(serviceName);
        //如果没有需要为这个服务创建selector
        if (selector==null){
            List<InetSocketAddress> serviceList = PrpcBootstrap.getInstance().getRegistry().lookUpService(serviceName,group);
            selector=getSelector(serviceList);
            cache.put(serviceName,selector);
        }

        return selector.getNext();
    }

    /**
     * 由子类拓展
     * @param serviceList
     * @return
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);


    @Override
    public void reLoadBalance(String serviceName, List<InetSocketAddress> addresses) {
        cache.put(serviceName,getSelector(addresses));
    }
}

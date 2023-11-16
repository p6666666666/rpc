package com.czp.loadbalancer.impl;

import com.czp.PrpcBootstrap;
import com.czp.discovery.Registry;
import com.czp.exception.LoadBalancerException;
import com.czp.loadbalancer.AbstractLoadBalancer;
import com.czp.loadbalancer.LoadBalancer;
import com.czp.loadbalancer.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询策略
 */
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {





    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }

    private static class RoundRobinSelector implements Selector{

        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if (serviceList==null||serviceList.size()==0){
                log.error("进行负载均衡选取节点时服务列表为空");
                throw new LoadBalancerException();
            }

            InetSocketAddress inetSocketAddress = serviceList.get(index.get());
            if (index.get()==serviceList.size()-1){
                index.set(0);
            }else{
                index.incrementAndGet();
            }

            return inetSocketAddress;
        }

    }
}

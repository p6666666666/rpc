package com.czp.loadbalancer.impl;

import com.czp.PrpcBootstrap;
import com.czp.loadbalancer.AbstractLoadBalancer;
import com.czp.loadbalancer.Selector;
import com.czp.transport.message.PrpcRequest;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Slf4j
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseTimeSelector(serviceList);
    }
    private static class  MinimumResponseTimeSelector implements Selector{
        public  MinimumResponseTimeSelector(List<InetSocketAddress> serviceList) {

        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = PrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if (entry!=null){
                log.info("最小响应负载均衡选取了【{}ms】响应时间的服务",entry.getKey());
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }
            //直接从缓存获取一个可用的服务
            Channel channel = (Channel) PrpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress) channel.remoteAddress();
        }

    }

}

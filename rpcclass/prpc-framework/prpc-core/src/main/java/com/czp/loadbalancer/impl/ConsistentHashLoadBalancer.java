package com.czp.loadbalancer.impl;

import com.czp.PrpcBootstrap;
import com.czp.exception.LoadBalancerException;
import com.czp.loadbalancer.AbstractLoadBalancer;
import com.czp.loadbalancer.Selector;
import com.czp.transport.message.PrpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询策略
 */
@Slf4j
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {





    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new  ConsistentHashSelector(serviceList,256);
    }

    private static class  ConsistentHashSelector implements Selector{

        //hash环
        private SortedMap<Integer,InetSocketAddress> circle=new TreeMap<>();
        //虚拟节点的个数
        private int virtualNodes;

        public  ConsistentHashSelector(List<InetSocketAddress> serviceList,int virtualNodes) {
            this.virtualNodes=virtualNodes;
            for(InetSocketAddress inetSocketAddress:serviceList){
                addNodeToCircle(inetSocketAddress);
            }
        }

        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            //挂载虚拟节点
            for (int i = 0; i < virtualNodes; i++) {
                int hash=hash(inetSocketAddress.toString()+"-"+i);
                circle.put(hash,inetSocketAddress);
                if (log.isDebugEnabled()){
                    log.debug("服务【{}】已经挂载到哈希环上",hash);
                }
            }

        }
        private void removeNodeFromCircle(InetSocketAddress inetSocketAddress) {
            for (int i = 0; i < virtualNodes; i++) {
                int hash=hash(inetSocketAddress.toString()+"-"+i);
                circle.remove(hash);
            }
        }



        @Override
        public InetSocketAddress getNext() {
            PrpcRequest prpcRequest = PrpcBootstrap.REQUEST_THREAD_LOCAL.get();


            String requestId = Long.toString(prpcRequest.getRequestId());

            //对请求的id做hash
            int hash = hash(requestId);
            //判断是否直接坐落服务器节点
            if (!circle.containsKey(hash)){
                //寻找最近的节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                hash=tailMap.isEmpty()?circle.firstKey():tailMap.firstKey();
            }
            return circle.get(hash);
        }
        private int hash(String s) {
            MessageDigest md;
            try {
                md=MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException();
            }
            byte[] digest = md.digest(s.getBytes());
            //获取int的四个字节
            int res=0;
            for (int i = 0; i < 4; i++) {
               res=res<<8;
               res=res|digest[i];
            }
            return res;
        }
    }
}

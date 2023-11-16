package com.czp.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡的接口
 */
public interface LoadBalancer {
    /**
     * 根据服务名获取可用服务
     * @param ServiceName
     * @return 服务地址
     */
    InetSocketAddress selectServiceAddress(String ServiceName,String group);
    void reLoadBalance(String serviceName, List<InetSocketAddress> addresses);
}

package com.czp.discovery;

import com.czp.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

public interface Registry {

    /**
     * 注册服务
     * @param serviceConfig
     */
    void register(ServiceConfig<?> serviceConfig,String group);

    /**
     * 拉取服务列表
     * @param interfaceName
     */
    List<InetSocketAddress> lookUpService(String interfaceName,String group);
}

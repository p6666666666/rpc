package com.czp.discovery;

import com.czp.ServiceConfig;

import java.net.InetSocketAddress;

public interface Registry {

    /**
     * 注册服务
     * @param serviceConfig
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 拉取服务
     * @param interfaceName
     */
    InetSocketAddress lookUpService(String interfaceName);
}

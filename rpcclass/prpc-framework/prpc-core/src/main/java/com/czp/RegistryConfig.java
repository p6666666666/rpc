package com.czp;

import com.czp.discovery.Registry;
import com.czp.discovery.impl.ZookeeperRegistry;
import com.czp.exception.DiscoveryException;

import java.util.Locale;

public class RegistryConfig {
    //连接的url
    private String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }
    public Registry getRegistry(){
        //1.获取注册中心的类型
        String registryType = getRegistryType(connectString.toLowerCase().trim(),true);

        if (registryType.equals("zookeeper")){
            String host=getRegistryType(connectString.toLowerCase().trim(),false);
            return new ZookeeperRegistry(host,Constant.DEFAULT_ZK_CONNECT_TIME_OUT);
        }
        throw new DiscoveryException("未发现合适注册中心");
    }

    /**
     * 获取注册中心类型和地址
     * @param connectString
     * @param isType true:获取type false:获取host
     * @return
     */
    private String getRegistryType(String connectString,boolean isType){
        String[] typeAndHost = connectString.split("://");
        if (typeAndHost.length!=2){
            throw  new RuntimeException("给定的注册中心连接url不合法");
        }
        if (isType){
            return typeAndHost[0];
        }else{
            return typeAndHost[1];
        }
    }
}

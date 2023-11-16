package com.czp;

public class Constant {
    //ZK默认地址
    public static final String DEFAULT_ZK_CONNECT="192.168.200.128:2181";
    //ZK链接超时事件
    public static final int DEFAULT_ZK_CONNECT_TIME_OUT=10000;

    //服务提供放和调用放在zookeeper注册中心的路径
    public static final String BASE_PROVIDERS_PATH="/prpc-metadata/providers";
    public static final String BASE_CONSUMERS_PATH="/prpc-metadata/consumers";
}

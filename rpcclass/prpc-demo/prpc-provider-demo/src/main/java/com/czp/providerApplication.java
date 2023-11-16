package com.czp;

import com.czp.impl.HelloRpcImpl;

public class providerApplication {
    public static void main(String[] args) {
        //封装服务
        ServiceConfig<HelloRpc> service =new ServiceConfig<>();
        service.setInterface(HelloRpc.class);
        service.setRef(new HelloRpcImpl());
        service.setGroup("main");
        //定义注册中心

        //通过启动引导程序，启动服务提供方
        PrpcBootstrap.getInstance()
                .application("first-prpc-provider")
                .registry(new RegistryConfig("zookeeper://192.168.200.128:2181"))
                .scan("com.czp")
                .start();

    }
}

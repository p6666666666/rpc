package com.czp;

import com.czp.impl.HelloRpcImpl;

public class providerApplication {
    public static void main(String[] args) {
        //封装服务
        ServiceConfig<HelloRpc> service =new ServiceConfig<>();
        service.setInterface(HelloRpc.class);
        service.setRef(new HelloRpcImpl());
        //定义注册中心

        //通过启动引导程序，启动服务提供方
        PrpcBootstrap.getInstance()
                .application("first-prpc-provider")
                .registry(new RegistryConfig("zookeeper://120.27.214.4:2181"))
                .protocol(new ProtocolConfig("jdk"))
                .publish(service)
                .start();

    }
}

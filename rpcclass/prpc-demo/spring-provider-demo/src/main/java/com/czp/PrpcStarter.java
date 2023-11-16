package com.czp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PrpcStarter implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        log.info("启动rpc");
        Thread.sleep(5000);
        //通过启动引导程序，启动服务提供方
        PrpcBootstrap.getInstance()
                .application("first-prpc-provider")
                // 配置注册中心
                .registry(new RegistryConfig("zookeeper://192.168.200.128:2181"))
                .serialize("jdk")
                .scan("com.czp")
                // 启动服务
                .start();
    }
}

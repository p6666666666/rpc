package com.czp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        ReferenceConfig<HelloRpc> reference =new ReferenceConfig<>();
        reference.setInterface(HelloRpc.class);
        PrpcBootstrap.getInstance()
                .application("first-prpc-consumer")
                .registry(new RegistryConfig("zookeeper://120.27.214.4:2181"))
                .serialize("hessian")
                .compress("gzip")
                .reference(reference);

        HelloRpc helloRpc=reference.get();
        String sayHi=helloRpc.sayHi("你好");
        log.info("sayHi-->{}",sayHi);
    }

}

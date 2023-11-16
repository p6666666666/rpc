package com.czp;

import com.czp.core.HeartbeatDetector;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        ReferenceConfig<HelloRpc> reference =new ReferenceConfig<>();
        reference.setInterface(HelloRpc.class);
        PrpcBootstrap.getInstance()
                .application("first-prpc-consumer")
                .registry(new RegistryConfig("zookeeper://192.168.200.128:2181"))
                .serialize("hessian")
                .compress("gzip")
                .group("default")
                .reference(reference);

        HelloRpc helloRpc=reference.get();
        while(true){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 500; i++) {
                String sayHi=helloRpc.sayHi("你好");
                log.info("sayHi-->{}",sayHi);
            }
        }





    }

}

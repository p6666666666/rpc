package com.czp;

public class Application {
    public static void main(String[] args) {
        ReferenceConfig<HelloRpc> reference =new ReferenceConfig<>();
        reference.setInterface(HelloRpc.class);
        PrpcBootstrap.getInstance()
                .application("first-prpc-consumer")
                .registry(new RegistryConfig("zookeeper://120.27.214.4:2181"))
                .reference(reference);

        HelloRpc helloRpc=reference.get();
    }

}

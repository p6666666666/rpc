package com.czp.proxy;

import com.czp.PrpcBootstrap;
import com.czp.ReferenceConfig;
import com.czp.RegistryConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyFactory {

    public static Map<Class<?>,Object> cache=new ConcurrentHashMap<>(32);

    public static <T> T getProxy(Class<T> clazz){
        Object bean = cache.get(clazz);
        if (bean!=null){
            return (T)bean;
        }
        ReferenceConfig<T> reference =new ReferenceConfig<>();
        reference.setInterface(clazz);
        PrpcBootstrap.getInstance()
                .application("first-prpc-consumer")
                .registry(new RegistryConfig("zookeeper://192.168.200.128:2181"))
                .serialize("hessian")
                .compress("gzip")
                .group("default")
                .reference(reference);

        T t=reference.get();
        cache.put(clazz,t);
        return t;
    }
}

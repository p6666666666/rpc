package com.czp;

import com.czp.discovery.Registry;
import com.czp.exception.NetWorkException;
import com.czp.proxy.handler.RpcConsumerInvocationHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    private Registry registry;

    private String group;

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }


    /**
     * 生成api接口的代理对象
     * @return
     */
    public T get() {
        //这里一定是使用动态代理
        ClassLoader classLoader=Thread.currentThread().getContextClassLoader();
        Class[] classes=new Class[]{interfaceRef};
        InvocationHandler handler=new RpcConsumerInvocationHandler(registry,interfaceRef,group);
        //使用动态代理生成代理对象
        Object helloProxy=Proxy.newProxyInstance(classLoader, classes, handler);
        return (T) helloProxy;
    }

    public void setGroup(String group) {
        this.group=group;
    }
}

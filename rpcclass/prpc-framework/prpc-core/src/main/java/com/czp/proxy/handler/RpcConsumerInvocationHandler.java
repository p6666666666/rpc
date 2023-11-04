package com.czp.proxy.handler;

import com.czp.NettyBootstrapInitializer;
import com.czp.PrpcBootstrap;
import com.czp.compress.CompressFactory;
import com.czp.discovery.Registry;
import com.czp.enumeration.RequestType;
import com.czp.exception.DiscoveryException;
import com.czp.exception.NetWorkException;
import com.czp.serializer.SerializerFactory;
import com.czp.transport.message.PrpcRequest;
import com.czp.transport.message.RequestPayload;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 客户端通信的基础逻辑
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {
    //需要一个注册中心
    private Registry registry;

    private Class<?> interfaceRef;
    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InetSocketAddress inetSocketAddress = registry.lookUpService(interfaceRef.getName());
        if (log.isDebugEnabled()){
            log.debug("{}发现服务的可用主机{}",interfaceRef.getName(),inetSocketAddress);
        }
        //netty
        Channel channel=getAvailableChannel(inetSocketAddress);

        RequestPayload requestPayload=RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue((args))
                .returnType(method.getReturnType())
                .build();
        //封装报文
        PrpcRequest prpcRequest = PrpcRequest.builder()
                .requestId(PrpcBootstrap.idGenerator.getId())
                .compressType(CompressFactory.getCompressor(PrpcBootstrap.COMPRESS_TYPE).getCode())
                .requestType(RequestType.REQUEST.getId())
                .serializeType(SerializerFactory.getSerializer(PrpcBootstrap.SERIALIZE_TYPE).getCode())
                .requestPayload(requestPayload)
                .build();


        CompletableFuture<Object> completableFuture=new CompletableFuture<>();
        PrpcBootstrap.PENDING_REQUEST.put(1L,completableFuture);
        channel.writeAndFlush(prpcRequest).addListener((ChannelFutureListener) promise->{
            if (!promise.isSuccess()){
                completableFuture.completeExceptionally(promise.cause());
            }
        });
        //这里会阻塞等待complete的执行。
        return completableFuture.get(10,TimeUnit.SECONDS);
    }

    /**
     * 根据地址获取通道
     * @param inetSocketAddress
     * @return
     */
    private Channel getAvailableChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = PrpcBootstrap.CHANNEL_CACHE.get(inetSocketAddress);
        if (channel==null){
            CompletableFuture<Channel> channelFuture=new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(inetSocketAddress).addListener(
                    (ChannelFutureListener) promise ->{
                        if (promise.isDone()){
                            if (log.isDebugEnabled()){
                                log.debug("已经和{}建立了连接",promise.channel());
                            }
                            channelFuture.complete(promise.channel());
                        }else if (!promise.isSuccess()){
                            channelFuture.isCompletedExceptionally();
                        }
                    }
            );
            try {
                channel=channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException |TimeoutException |ExecutionException e) {
                log.error("获取通道时，发生异常",e);
                throw  new DiscoveryException();
            }
            //缓存channel
            PrpcBootstrap.CHANNEL_CACHE.put(inetSocketAddress,channel);
        }
        if (channel==null){
            throw new NetWorkException("获取通道异常");
        }
        return channel;
    }
}

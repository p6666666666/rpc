package com.czp.proxy.handler;

import com.czp.NettyBootstrapInitializer;
import com.czp.PrpcBootstrap;
import com.czp.annotation.PrpcApi;
import com.czp.annotation.TryCount;
import com.czp.compress.CompressFactory;
import com.czp.discovery.Registry;
import com.czp.enumeration.RequestType;
import com.czp.exception.DiscoveryException;
import com.czp.exception.NetWorkException;
import com.czp.protection.CircuitBreaker;
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
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
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
    private String group;
    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef,String group) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
        this.group=group;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        //获取@TryCount的值
        TryCount tryCountAnnotation = method.getAnnotation(TryCount.class);
        //默认值
        int tryCount=0;
        int intervalTime=0;
        if (tryCountAnnotation!=null){
            tryCount=tryCountAnnotation.tryCount();
            intervalTime=tryCountAnnotation.intervalTime();
        }
        while(true) {
           RequestPayload requestPayload = RequestPayload.builder()
                        .interfaceName(interfaceRef.getName())
                        .methodName(method.getName())
                        .parametersType(method.getParameterTypes())
                        .parametersValue((args))
                        .returnType(method.getReturnType())
                        .build();
           //封装报文
           PrpcRequest prpcRequest = PrpcRequest.builder()
                        .requestId(PrpcBootstrap.getInstance().getConfiguration().idGenerator.getId())
                        .compressType(CompressFactory.getCompressor(PrpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                        .requestType(RequestType.REQUEST.getId())
                        .serializeType(SerializerFactory.getSerializer(PrpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                        .timeStamp(new Date().getTime())
                        .requestPayload(requestPayload)
                        .build();

           //将请求存入本地线程，需要在合适的时候调用remove
           PrpcBootstrap.REQUEST_THREAD_LOCAL.set(prpcRequest);

           //拉取服务列表并通过负载均衡策略获取服务地址
           InetSocketAddress inetSocketAddress = PrpcBootstrap.getInstance().getConfiguration().getLoadBalancer().selectServiceAddress(interfaceRef.getName(),group);
           if (log.isDebugEnabled()) {
               log.debug("{}发现服务的可用主机{}", interfaceRef.getName(), inetSocketAddress);
           }
           //配置熔断器
           CircuitBreaker circuitBreaker = PrpcBootstrap.CIRCUIT_BREAKER_MAP.get(inetSocketAddress);
           if (circuitBreaker==null){
               circuitBreaker = new CircuitBreaker(10,0.5F);
               PrpcBootstrap.CIRCUIT_BREAKER_MAP.put(inetSocketAddress,circuitBreaker);
           }
            try {
                if (prpcRequest.getRequestType()!=RequestType.HEART_BRAT.getId()&&circuitBreaker.isBreak()){
                    Timer timer=new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            PrpcBootstrap.CIRCUIT_BREAKER_MAP.get(inetSocketAddress).reset();
                        }
                    },5000);
                    log.info("请求被熔断器拦截");
                    throw new RuntimeException("当前异常过多，熔断器已经打开");
                }
                //netty获取通道
                Channel channel = getAvailableChannel(inetSocketAddress);
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                PrpcBootstrap.PENDING_REQUEST.put(prpcRequest.getRequestId(), completableFuture);
                channel.writeAndFlush(prpcRequest).addListener((ChannelFutureListener) promise -> {
                    if (!promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });
                PrpcBootstrap.REQUEST_THREAD_LOCAL.remove();
                //这里会阻塞等待complete的执行。
                Object result = completableFuture.get(10, TimeUnit.SECONDS);
                circuitBreaker.recordRequest();
                return result;
            }catch (Exception e){
                tryCount--;
                circuitBreaker.recordErrorRequest();
                circuitBreaker.recordRequest();
                try {
                    Thread.sleep(intervalTime);
                }catch (InterruptedException ex){
                    log.error("重试时发生异常");
                }
                if (tryCount<0){
                    log.error("【{}】方法调用失败,重试仍然不可调用",method.getName());
                    break;
                }
                log.error("【{}】方法调用时发生异常，正在进行重试",method.getName());
            }
        }
        throw new RuntimeException(method.getName()+"方法调用失败");
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

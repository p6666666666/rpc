package com.czp.core;

import com.czp.NettyBootstrapInitializer;
import com.czp.PrpcBootstrap;
import com.czp.compress.CompressFactory;
import com.czp.discovery.Registry;
import com.czp.enumeration.RequestType;
import com.czp.serializer.SerializerFactory;
import com.czp.transport.message.PrpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class HeartbeatDetector {

    public static void detectHeartbeat(String ServiceName){
        //从注册中心拉取服务列表并建立连接
        Registry registry = PrpcBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> addresses = registry.lookUpService(ServiceName,PrpcBootstrap.getInstance().getConfiguration().getGroup());
        for (InetSocketAddress address:addresses){
            try{
                if (!PrpcBootstrap.CHANNEL_CACHE.containsKey(address)){
                    Channel channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    PrpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            }catch (InterruptedException e){
                throw new RuntimeException(e);
            }
        }
        Thread thread=new Thread(()->{
            new Timer().scheduleAtFixedRate(new MyTimerTask(),0,2000);
        },"prpc-HeartbeatDetector-thread");
        thread.setDaemon(true);
        thread.start();


    }
    private static class MyTimerTask extends TimerTask{

        @Override
        public void run() {
            //将响应的时长清空
            PrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();
            Map<InetSocketAddress, Channel> cache = PrpcBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress, Channel> entry: cache.entrySet() ){
                int timeoutCount=3;
                while(timeoutCount>0){
                    Channel channel = entry.getValue();
                    long start = System.currentTimeMillis();
                    PrpcRequest prpcRequest = PrpcRequest.builder()
                            .requestId(PrpcBootstrap.getInstance().getConfiguration().idGenerator.getId())
                            .compressType(CompressFactory.getCompressor(PrpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                            .requestType(RequestType.HEART_BRAT.getId())
                            .serializeType(SerializerFactory.getSerializer(PrpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                            .timeStamp(start)
                            .build();
                    CompletableFuture<Object> completableFuture=new CompletableFuture<>();
                    PrpcBootstrap.PENDING_REQUEST.put(prpcRequest.getRequestId(),completableFuture);
                    channel.writeAndFlush(prpcRequest).addListener((ChannelFutureListener) promise->{
                        if (!promise.isSuccess()){
                            completableFuture.completeExceptionally(promise.cause());
                        }
                    });
                    Long endTime=0L;
                    try {
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime=System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        timeoutCount--;
                        log.error("与地址为【{}】的主机连接发生异常,进行第【{}】次重连",channel.remoteAddress(),3-timeoutCount);
                        if (timeoutCount==0){
                            PrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }
                        continue;
                    }
                    long time = endTime - start;
                    //缓存响应时间
                    PrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time,channel);
                    break;
                }
            }
            log.info("响应时间——————————————————————");
            for (Map.Entry<Long,Channel> entry:PrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.entrySet()){
                if (log.isDebugEnabled()){
                    log.debug("[{}]---->channelId{[]}",entry.getKey(),entry.getValue().id());
                }
            }


        }
    }
}

package com.czp.channnelHandler.handler;

import com.czp.PrpcBootstrap;
import com.czp.enumeration.ResponseCode;
import com.czp.exception.ResponseException;
import com.czp.loadbalancer.LoadBalancer;
import com.czp.protection.CircuitBreaker;
import com.czp.transport.message.PrpcRequest;
import com.czp.transport.message.PrpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<PrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, PrpcResponse prpcResponse) throws Exception {

        CompletableFuture<Object> completableFuture = PrpcBootstrap.PENDING_REQUEST.get(prpcResponse.getRequestId());

        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        CircuitBreaker circuitBreaker = PrpcBootstrap.CIRCUIT_BREAKER_MAP.get(socketAddress);

        byte code = prpcResponse.getCode();
        if (code==ResponseCode.SUCCESS.getCode()){
            Object returnValue= prpcResponse.getBody();
            completableFuture.complete(returnValue);
            log.info("成功处理请求为【{}】的响应结果",prpcResponse.getRequestId());
        }else if (code==ResponseCode.FAIL.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，返回错误的结果，响应码[{}].",
                    prpcResponse.getRequestId(),prpcResponse.getCode());
            throw new ResponseException(code,ResponseCode.FAIL.getDesc());
        }else if (code==ResponseCode.RATE_LIMIT.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，请求被限流，响应码[{}].",
                    prpcResponse.getRequestId(),prpcResponse.getCode());
            throw new ResponseException(code,ResponseCode.RATE_LIMIT.getDesc());
        }else if (code==ResponseCode.RESOURCE_NOT_FOUND.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，找不到资源，响应码[{}].",
                    prpcResponse.getRequestId(),prpcResponse.getCode());
            throw new ResponseException(code,ResponseCode.RESOURCE_NOT_FOUND.getDesc());
        }else if(code == ResponseCode.SUCCESS_HEART_BEAT.getCode()) {
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("以寻找到编号为【{}】的completableFuture,处理心跳检测，处理响应结果。", prpcResponse.getRequestId());
            }
        } else if (code==ResponseCode.BECLOSING.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("当前id为[{}]的请求，访问被拒绝，目标服务器正处于关闭中，响应码[{}].",
                        prpcResponse.getRequestId(),prpcResponse.getCode());
            }
            // 修正负载均衡器
            // 从健康列表中移除
            PrpcBootstrap.CHANNEL_CACHE.remove(socketAddress);
            // reLoadBalance
            LoadBalancer loadBalancer = PrpcBootstrap.getInstance()
                    .getConfiguration().getLoadBalancer();
            // 重新进行负载均衡
            PrpcRequest prpcRequest = PrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            loadBalancer.reLoadBalance(prpcRequest.getRequestPayload().getInterfaceName()
                    ,PrpcBootstrap.CHANNEL_CACHE.keySet().stream().collect(Collectors.toList()));
            throw new ResponseException(code,ResponseCode.BECLOSING.getDesc());
        }





    }
}

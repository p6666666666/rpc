package com.czp.channnelHandler.handler;

import com.czp.PrpcBootstrap;
import com.czp.ServiceConfig;
import com.czp.enumeration.RequestType;
import com.czp.enumeration.ResponseCode;
import com.czp.hook.ShutDownHolder;
import com.czp.protection.RateLimiter;
import com.czp.protection.TokenBucketRateLimiter;
import com.czp.transport.message.PrpcRequest;
import com.czp.transport.message.PrpcResponse;
import com.czp.transport.message.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<PrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, PrpcRequest prpcRequest) throws Exception {
        //封装响应
        PrpcResponse prpcResponse = new PrpcResponse();
        prpcResponse.setRequestId(prpcRequest.getRequestId());
        prpcResponse.setCompressType(prpcRequest.getCompressType());
        prpcResponse.setSerializeType(prpcRequest.getSerializeType());

        Channel channel = channelHandlerContext.channel();

        if (ShutDownHolder.BAFFLE.get()){
            prpcResponse.setCode(ResponseCode.BECLOSING.getCode());
            channel.writeAndFlush(prpcResponse);
            return;
        }
        //计数器加一
        ShutDownHolder.REQUEST_COUNTER.increment();

        //限流器配置
        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        RateLimiter rateLimiter = PrpcBootstrap.RATE_LIMITER_MAP.get(socketAddress);
        if (rateLimiter==null){
            rateLimiter = new TokenBucketRateLimiter(10,10);
            PrpcBootstrap.RATE_LIMITER_MAP.put((InetSocketAddress) socketAddress,rateLimiter);
        }
        boolean flag = rateLimiter.allowRequest();
        //根据请求类型处理响应
        if (!flag){
            prpcResponse.setCode(ResponseCode.RATE_LIMIT.getCode());
        }else if (prpcRequest.getRequestType()==RequestType.HEART_BRAT.getId()){
            prpcResponse.setCode(ResponseCode.SUCCESS_HEART_BEAT.getCode());
        }else{
            //获取负载内容
            RequestPayload requestPayload = prpcRequest.getRequestPayload();
            //根据负载内容进行方法调用
            Object result=null;
            try {
                result = callTargetMethod(requestPayload);
                if (log.isDebugEnabled()) {
                    log.debug("请求【{}】已经在服务端完成方法调用", prpcRequest.getRequestId());
                }

                //封装响应
                prpcResponse.setCode(ResponseCode.SUCCESS.getCode());
                prpcResponse.setTimeStamp(prpcRequest.getTimeStamp());
                prpcResponse.setBody(result);
            }catch (Exception e){
                log.error("编号为【{}】的请求在调用过程中发生异常",prpcRequest.getRequestId(),e);
                prpcResponse.setCode(ResponseCode.FAIL.getCode());
            }
        }
        //写出响应
        channel.writeAndFlush(prpcResponse);
        ShutDownHolder.REQUEST_COUNTER.decrement();

    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName=requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();

        //寻找匹配暴露的具体实现
        ServiceConfig<?> serviceConfig = PrpcBootstrap.SERVICE_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        //通过反射调用
        Class<?> aClass = refImpl.getClass();
        Object  returnValue=null;
        try {
             Method method = aClass.getMethod(methodName, parametersType);
             returnValue= method.invoke(refImpl, parametersValue);
        } catch (NoSuchMethodException | InvocationTargetException|IllegalAccessException e) {
            log.error("调用服务的【{}】的方法【{}】时发生异常",interfaceName,methodName,e);
            throw new RuntimeException(e);
        }
        return returnValue;



    }
}

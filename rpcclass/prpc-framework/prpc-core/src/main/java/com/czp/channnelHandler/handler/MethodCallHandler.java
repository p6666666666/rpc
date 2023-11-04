package com.czp.channnelHandler.handler;

import com.czp.PrpcBootstrap;
import com.czp.ServiceConfig;
import com.czp.enumeration.ResponseCode;
import com.czp.transport.message.PrpcRequest;
import com.czp.transport.message.PrpcResponse;
import com.czp.transport.message.RequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<PrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, PrpcRequest prpcRequest) throws Exception {
        //获取负载内容
        RequestPayload requestPayload = prpcRequest.getRequestPayload();
        //根据负载内容进行方法调用
        Object result=callTargetMethod(requestPayload);
        if (log.isDebugEnabled()){
            log.debug("请求【{}】已经在服务端完成方法调用",prpcRequest.getRequestId());
        }

        //封装响应
        PrpcResponse prpcResponse = new PrpcResponse();
        prpcResponse.setCode(ResponseCode.SUCCESS.getCode());
        prpcResponse.setRequestId(prpcRequest.getRequestId());
        prpcResponse.setCompressType(prpcRequest.getCompressType());
        prpcResponse.setSerializeType(prpcRequest.getSerializeType());
        prpcResponse.setBody(result);

        //写出响应
        channelHandlerContext.channel().writeAndFlush(prpcResponse);
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

package com.czp.channnelHandler.handler;

import com.czp.PrpcBootstrap;
import com.czp.transport.message.PrpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<PrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, PrpcResponse prpcResponse) throws Exception {
        Object returnValue= prpcResponse.getBody();
        CompletableFuture<Object> completableFuture = PrpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(returnValue);
        if (log.isDebugEnabled()){
            log.debug("【{}】处理响应结果",prpcResponse.getRequestId());
        }
    }
}

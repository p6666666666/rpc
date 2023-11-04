package com.czp.channnelHandler;

import com.czp.channnelHandler.handler.MySimpleChannelInboundHandler;
import com.czp.channnelHandler.handler.PrpcRequestEncoder;
import com.czp.channnelHandler.handler.PrpcResponseDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                //netty自带的日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new PrpcRequestEncoder())
                .addLast(new PrpcResponseDecoder())
                //处理结果
                .addLast(new MySimpleChannelInboundHandler());
    }
}

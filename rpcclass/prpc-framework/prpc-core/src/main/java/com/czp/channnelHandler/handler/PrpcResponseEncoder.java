package com.czp.channnelHandler.handler;

import com.czp.compress.CompressFactory;
import com.czp.compress.Compressor;
import com.czp.serializer.Serializer;
import com.czp.serializer.SerializerFactory;
import com.czp.transport.message.MessageFormatConstant;
import com.czp.transport.message.PrpcResponse;
import com.czp.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Slf4j
public class PrpcResponseEncoder extends MessageToByteEncoder<PrpcResponse> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, PrpcResponse prpcResponse, ByteBuf byteBuf) throws Exception {
        //魔术值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        //1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        //2个字节的头部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        //留出总长度位置
        byteBuf.writerIndex(byteBuf.writerIndex()+MessageFormatConstant.FULL_LENGTH_LENGTH);
        //3个类型
        byteBuf.writeByte(prpcResponse.getCode());
        byteBuf.writeByte(prpcResponse.getSerializeType());
        byteBuf.writeByte(prpcResponse.getCompressType());
        //8字节请求id
        byteBuf.writeLong(prpcResponse.getRequestId());
        byteBuf.writeLong(prpcResponse.getTimeStamp());

        //写入请求体
        byte[] body=null;
        if (prpcResponse.getBody()!=null){
            Serializer serializer= SerializerFactory.getSerializer(prpcResponse.getSerializeType()).getImpl();
            body = serializer.serialize(prpcResponse.getBody());
            // 压缩
            Compressor compressor = CompressFactory.getCompressor(prpcResponse.getCompressType()).getImpl();
            body=compressor.compress(body);
        }


        if (body!=null){
            byteBuf.writeBytes(body);
        }
        int bodyLength=body==null?0:body.length;
        //总长度
        int writerIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length+
                MessageFormatConstant.VERSION_LENGTH+MessageFormatConstant.HEADER_LENGTH_LENGTH
        );
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH+bodyLength);

        //写指针归位
        byteBuf.writerIndex(writerIndex);
        if (log.isDebugEnabled()){
            log.debug("请求【{}】已经在服务端完成响应编码工作",prpcResponse.getRequestId());
        }
    }

}

package com.czp.channnelHandler.handler;

import com.czp.PrpcBootstrap;
import com.czp.compress.CompressFactory;
import com.czp.compress.Compressor;
import com.czp.serializer.SerializerFactory;
import com.czp.transport.message.MessageFormatConstant;
import com.czp.transport.message.PrpcRequest;
import com.czp.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 出站时第一个处理器
 */
@Slf4j
public class PrpcRequestEncoder extends MessageToByteEncoder<PrpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, PrpcRequest prpcRequest, ByteBuf byteBuf) throws Exception {
        //魔术值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        //1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        //2个字节的头部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        //留出总长度位置
        byteBuf.writerIndex(byteBuf.writerIndex()+MessageFormatConstant.FULL_LENGTH_LENGTH);
        //3个类型
        byteBuf.writeByte(prpcRequest.getRequestType());
        byteBuf.writeByte(prpcRequest.getSerializeType());
        byteBuf.writeByte(prpcRequest.getCompressType());


        //8字节请求id
        byteBuf.writeLong(prpcRequest.getRequestId());

        //写入请求体
        //序列化
        Serializer serializer= SerializerFactory.getSerializer(prpcRequest.getSerializeType()).getSerializer();
        byte[] body= serializer.serialize(prpcRequest.getRequestPayload());
        //压缩
        Compressor compressor = CompressFactory.getCompressor(prpcRequest.getCompressType()).getCompressor();
        body=compressor.compress(body);
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
            log.debug("请求【{}】已经完成报文的编码",prpcRequest.getRequestId());
        }
    }


}

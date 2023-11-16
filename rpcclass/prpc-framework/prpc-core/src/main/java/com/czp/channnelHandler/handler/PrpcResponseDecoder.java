package com.czp.channnelHandler.handler;

import com.czp.compress.CompressFactory;
import com.czp.compress.Compressor;
import com.czp.enumeration.RequestType;
import com.czp.serializer.Serializer;
import com.czp.serializer.SerializerFactory;
import com.czp.transport.message.MessageFormatConstant;
import com.czp.transport.message.PrpcRequest;
import com.czp.transport.message.PrpcResponse;
import com.czp.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

@Slf4j
public class PrpcResponseDecoder extends LengthFieldBasedFrameDecoder {

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf ){
            ByteBuf byteBuf=(ByteBuf) decode;
            return decodeFrame(byteBuf);
        }
        return null;

    }

    private Object decodeFrame(ByteBuf byteBuf) {
        //解析魔数值
        byte[] magic=new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        //检测魔术值是否匹配
        for (int i = 0; i < magic.length; i++) {
            if (magic[i]!=MessageFormatConstant.MAGIC[i]){
                throw new RuntimeException("获得的请求不合法");
            }
        }
        //解析版本号
        byte version = byteBuf.readByte();
        if (version>MessageFormatConstant.VERSION){
            throw new RuntimeException("获得的请求版本不被支持");
        }
        //解释头部长度
        short headLength = byteBuf.readShort();

        //解析总长度
        int fullLength = byteBuf.readInt();

        //响应代码
        byte responseCode=byteBuf.readByte();

        //序列化类型
        byte serializeType = byteBuf.readByte();

        //压缩类型
        byte compressType = byteBuf.readByte();

        //请求id
        long requestId = byteBuf.readLong();

        //时间戳
        long timeStamp = byteBuf.readLong();

        //封装
        PrpcResponse prpcResponse = new PrpcResponse();
        prpcResponse.setCode(responseCode);
        prpcResponse.setSerializeType(serializeType);
        prpcResponse.setCompressType(compressType);
        prpcResponse.setRequestId(requestId);
        prpcResponse.setTimeStamp(timeStamp);

        int bodyLength=fullLength-headLength;
        byte[] payload=new byte[bodyLength];
        byteBuf.readBytes(payload);

        if (payload!=null &&payload.length>0){
            // 解压缩
            Compressor compressor = CompressFactory.getCompressor(prpcResponse.getCompressType()).getImpl();
            payload = compressor.decompress(payload);
            //  反序列化
            Serializer serializer = SerializerFactory.getSerializer(serializeType).getImpl();
            Object body = serializer.deserialize(payload, Object.class);
            prpcResponse.setBody(body);
        }

        if (log.isDebugEnabled()){
            log.debug("响应【{}】已经在调用端晚成解码工作",requestId);
        }
        return prpcResponse;


    }

    public PrpcResponseDecoder() {
        super(
                //最大帧的长度，超过这个maxFrameLength值会直接丢弃
                MessageFormatConstant.MAX_FRAME_LENGTH,
                //长度字段的偏移量
                MessageFormatConstant.MAGIC.length+MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_LENGTH_LENGTH,
                //长度的字段的长度
                MessageFormatConstant.FULL_LENGTH_LENGTH,
                //负载的适配的长度
                -(MessageFormatConstant.MAGIC.length+MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_LENGTH_LENGTH+MessageFormatConstant.FULL_LENGTH_LENGTH),0);



    }
}

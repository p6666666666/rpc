package com.czp.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrpcResponse {

    //请求id
    private long requestId;
    //请求的类型,压缩的类型,序列化的方式
    private byte compressType;
    private byte serializeType;
    private long timeStamp;

    //响应码 1:成功 2:异常
    private byte code;
    //消息体
    private Object body;
}

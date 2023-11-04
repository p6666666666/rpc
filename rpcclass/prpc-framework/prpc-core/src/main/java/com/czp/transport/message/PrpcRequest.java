package com.czp.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrpcRequest {

    //请求id
    private long requestId;
    //请求的类型,压缩的类型,序列化的方式
    private byte requestType;
    private byte compressType;
    private byte serializeType;

    //消息体
    private RequestPayload requestPayload;
}

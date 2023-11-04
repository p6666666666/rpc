package com.czp.serializer.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.czp.exception.SerializeException;
import com.czp.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        //序列化
        byte[] bytes = JSON.toJSONBytes(object);
        if (log.isDebugEnabled()) {
            log.debug("类【{}】完成json序列化操作");
        }
        return bytes;


    }



    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes==null||clazz==null){
            return null;
        }
        T t = JSON.parseObject(bytes,clazz);
        if (log.isDebugEnabled()){
            log.debug("类【{}】完成json反序列化操作");
        }
        return t;

    }
}

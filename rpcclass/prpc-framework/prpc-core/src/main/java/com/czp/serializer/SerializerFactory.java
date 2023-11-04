package com.czp.serializer;

import com.czp.serializer.impl.HessianSerializer;
import com.czp.serializer.impl.JdkSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SerializerFactory {

    private final static ConcurrentHashMap<String,SerializerWrapper> SERIALIZER_CACHE=new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte,SerializerWrapper> SERIALIZER_CACHE_CODE=new ConcurrentHashMap<>(8);
    static{
        SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JdkSerializer());
        SerializerWrapper hessian = new SerializerWrapper((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk",jdk);
        SERIALIZER_CACHE.put("json",json);
        SERIALIZER_CACHE.put("hessian",hessian);
        SERIALIZER_CACHE_CODE.put((byte)1,jdk);
        SERIALIZER_CACHE_CODE.put((byte)2,json);
        SERIALIZER_CACHE_CODE.put((byte)3,hessian);

    }

    /**
     * 工厂方法获取一个SerializerWrapper
     * @param serializeType
     * @return
     */
    public static SerializerWrapper getSerializer(String serializeType) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE.get(serializeType);
        if (serializerWrapper==null){
            log.error("未找到你配置的【{}】序列化策略，默认选用jdk",serializeType);
            return SERIALIZER_CACHE.get("jdk");
        }

        return serializerWrapper;
    }
    public static SerializerWrapper getSerializer(Byte serializeCode) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        if (serializerWrapper==null){
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }

}

package com.czp.serializer;

import com.czp.compress.Compressor;
import com.czp.config.ObjectWrapper;
import com.czp.serializer.impl.HessianSerializer;
import com.czp.serializer.impl.JdkSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SerializerFactory {

    private final static ConcurrentHashMap<String, ObjectWrapper<Serializer>> SERIALIZER_CACHE=new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte,ObjectWrapper<Serializer>> SERIALIZER_CACHE_CODE=new ConcurrentHashMap<>(8);


    /**
     * 工厂方法获取一个SerializerWrapper
     * @param serializeType
     * @return
     */
    public static ObjectWrapper<Serializer> getSerializer(String serializeType) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE.get(serializeType);
        if (serializerWrapper==null){
            log.error("未找到你配置的【{}】序列化策略，默认选用jdk",serializeType);
            return SERIALIZER_CACHE.get("jdk");
        }

        return serializerWrapper;
    }
    public static ObjectWrapper<Serializer> getSerializer(Byte serializeCode) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        if (serializerWrapper==null){
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }
    public static void addSerializer(ObjectWrapper<Serializer> objectWrapper){
        SERIALIZER_CACHE.put(objectWrapper.getType(),objectWrapper);
        SERIALIZER_CACHE_CODE.put(objectWrapper.getCode(),objectWrapper);
    }

}

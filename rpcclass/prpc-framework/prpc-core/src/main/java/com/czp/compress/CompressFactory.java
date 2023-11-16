package com.czp.compress;

import com.czp.compress.impl.GzipCompressor;
import com.czp.config.ObjectWrapper;
import com.czp.serializer.SerializerWrapper;
import com.czp.serializer.impl.HessianSerializer;
import com.czp.serializer.impl.JdkSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompressFactory {

    private final static ConcurrentHashMap<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE=new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte,ObjectWrapper<Compressor>> COMPRESSOR_CACHE_CODE=new ConcurrentHashMap<>(8);

    /**
     * 工厂方法获取一个CompressWrapper
     * @param compressType)
     * @return
     */
    public static ObjectWrapper<Compressor> getCompressor(String compressType) {
        ObjectWrapper<Compressor> wrapper = COMPRESSOR_CACHE.get(compressType);
        if (wrapper==null){
            log.error("未找到你配置的【{}】压缩策略，默认选用gzip",compressType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return wrapper;
    }
    public static ObjectWrapper<Compressor> getCompressor(Byte compressCode) {
        ObjectWrapper<Compressor> wrapper = COMPRESSOR_CACHE_CODE.get(compressCode);
        if (wrapper==null){
            return COMPRESSOR_CACHE.get("gzip");
        }
        return wrapper;

    }
    public static void addCompressor(ObjectWrapper<Compressor> objectWrapper){
        COMPRESSOR_CACHE.put(objectWrapper.getType(),objectWrapper);
        COMPRESSOR_CACHE_CODE.put(objectWrapper.getCode(),objectWrapper);
    }

}

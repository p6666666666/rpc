package com.czp.compress;

import com.czp.compress.impl.GzipCompressor;
import com.czp.serializer.SerializerWrapper;
import com.czp.serializer.impl.HessianSerializer;
import com.czp.serializer.impl.JdkSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompressFactory {

    private final static ConcurrentHashMap<String, CompressWrapper> COMPRESSOR_CACHE=new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte,CompressWrapper> COMPRESSOR_CACHE_CODE=new ConcurrentHashMap<>(8);
    static{
        CompressWrapper gzip = new CompressWrapper((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip",gzip);

        COMPRESSOR_CACHE_CODE.put((byte)1,gzip);


    }

    /**
     * 工厂方法获取一个CompressWrapper
     * @param compressType)
     * @return
     */
    public static CompressWrapper getCompressor(String compressType) {
        CompressWrapper compressWrapper = COMPRESSOR_CACHE.get(compressType);
        if (compressWrapper==null){
            log.error("未找到你配置的【{}】压缩策略，默认选用gzip",compressType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressWrapper;
    }
    public static CompressWrapper getCompressor(Byte compressCode) {
        CompressWrapper compressWrapper = COMPRESSOR_CACHE_CODE.get(compressCode);
        if (compressWrapper==null){
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressWrapper;

    }

}

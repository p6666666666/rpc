package com.czp.serializer;

public interface Serializer {
    /**
     * 序列化
     * @param object
     * @return
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     * @param bytes
     * @param clazz 目标类
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] bytes,Class<T> clazz);
}

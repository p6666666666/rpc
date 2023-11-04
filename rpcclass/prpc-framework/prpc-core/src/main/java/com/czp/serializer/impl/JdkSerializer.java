package com.czp.serializer.impl;

import com.czp.exception.SerializeException;
import com.czp.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object==null){
            return null;
        }
        //序列化
        try(
                //自动关流
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                ObjectOutputStream outputStream=new ObjectOutputStream(baos);
                ){
            outputStream.writeObject(object);
            //压缩
            return baos.toByteArray();
        }catch (IOException e){
            log.error("序列化对象【{}】出现异常",object);
            throw new SerializeException();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes==null||clazz==null){
            return null;
        }
        try(
                //自动关流
                ByteArrayInputStream bais=new ByteArrayInputStream(bytes);
                ObjectInputStream inputStream=new ObjectInputStream(bais);
        ){
            Object object = inputStream.readObject();
            if (log.isDebugEnabled()){
                log.debug("类【{}】完成反序列化操作");
            }
            return (T)object;
        }catch (IOException | ClassNotFoundException e){
            log.error("反序列化对象【{}】出现异常",clazz);
            throw new SerializeException();
        }
    }
}

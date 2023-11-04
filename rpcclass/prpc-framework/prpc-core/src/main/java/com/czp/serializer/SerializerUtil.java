package com.czp.serializer;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Slf4j
public class SerializerUtil {
    public static byte[] serializer(Object object) {
        if (object==null){
            return null;
        }
        //序列化
        try{
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            ObjectOutputStream outputStream=new ObjectOutputStream(baos);
            outputStream.writeObject(object);
            //压缩
            return baos.toByteArray();
        }catch (IOException e){
            log.error("序列化时出现异常");
            throw new RuntimeException();
        }

    }
}

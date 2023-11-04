package com.czp.serializer.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.czp.exception.SerializeException;
import com.czp.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        //序列化
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ) {

            Hessian2Output hessian2Output = new Hessian2Output(baos);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            if (log.isDebugEnabled()) {
                log.debug("类【{}】完成hessian序列化操作");
            }
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("hessian序列化对象【{}】出现异常", object);
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
        ){
            Hessian2Input hessian2Input=new Hessian2Input(bais);
            T t = (T)hessian2Input.readObject();
            if (log.isDebugEnabled()){
                log.debug("类【{}】完成hessian反序列化操作");
            }
            return t;
        }catch (IOException  e){
            log.error("hessian反序列化对象【{}】出现异常",clazz);
            throw new SerializeException();
        }
    }
}

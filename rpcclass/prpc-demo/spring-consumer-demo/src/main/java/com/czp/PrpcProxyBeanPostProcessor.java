package com.czp;

import com.czp.annotation.PrpcBean;
import com.czp.proxy.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class PrpcProxyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] Fields = bean.getClass().getDeclaredFields();
        for (Field field:Fields){
            PrpcBean prpcBean = field.getAnnotation(PrpcBean.class);
            if (prpcBean!=null){
                Class<?> type=field.getType();
                Object proxy = ProxyFactory.getProxy(type);
                field.setAccessible(true);
                try {
                    field.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException();
                }
            }
        }
        return bean;
    }

}

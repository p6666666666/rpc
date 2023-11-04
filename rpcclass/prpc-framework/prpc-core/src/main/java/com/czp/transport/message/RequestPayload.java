package com.czp.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 描述consumer请求的接口方法
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {
    //接口名字
    private String interfaceName;
    //调用方法名字
    private String methodName;
    //方法参数列表
    private Class[] parametersType;
    private Object[] parametersValue;
    //返回值类型
    private  Class<?> returnType;
}

package com.czp;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;

@Slf4j
public class PrpcBootstrap {


    //单例模式
    private static PrpcBootstrap prpcBootstrap=new PrpcBootstrap();
    private PrpcBootstrap(){

    }
    public static  PrpcBootstrap getInstance(){
        return  prpcBootstrap;
    }

    /**
     * 定义应用的名字
     * @param appName 应用的名字
     * @return this当前实例
     */
    public PrpcBootstrap application(String appName){
        return  this;
    }

    /**
     * 配置配置中心
     * @param registryConfig
     * @return
     */
    public PrpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 配置使用的协议
     * @param protocolConfig 协议的封装
     * @return this当前实例
     */
    public PrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        if (log.isDebugEnabled()){
            log.debug("当前工程使用了：{}协议进行序列化",protocolConfig.toString());
        }
        return this;
    }

    /**
     * 发布服务
     * @param service 封装需要发布的服务
     * @return this当前实例
     */
    public PrpcBootstrap publish(ServiceConfig<?> service){
        if (log.isDebugEnabled()){
            log.debug("服务{}，已经被注册",service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量发布
     * @param services
     * @return
     */
    public PrpcBootstrap publish(List<?> services){
        return null;
    }

    /**
     * 启动netty服务
     */
    public void start(){

    }

    //---------------------------------consume------------------------
    public PrpcBootstrap reference(ReferenceConfig<?> reference) {
        return this;
    }
}

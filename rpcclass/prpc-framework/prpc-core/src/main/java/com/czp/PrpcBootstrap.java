package com.czp;

import com.czp.channnelHandler.handler.MethodCallHandler;
import com.czp.channnelHandler.handler.PrpcRequestDecoder;
import com.czp.channnelHandler.handler.PrpcResponseEncoder;
import com.czp.discovery.Registry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PrpcBootstrap {



    //单例模式
    private static PrpcBootstrap prpcBootstrap=new PrpcBootstrap();

    private String applicationName="default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port=8088;
    //注册中心
    private Registry registry;

    //连接的缓存
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE=new ConcurrentHashMap<>();

    public static final Map<String,ServiceConfig<?>> SERVICE_LIST=new HashMap<>(16);
    //定义全局的对外挂起的completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST=new ConcurrentHashMap<>();
    //全局的id生成器
    public static final IdGenerator idGenerator=new IdGenerator(1,2);
    public static String SERIALIZE_TYPE="jdk";
    public static String COMPRESS_TYPE = "gzip";
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
        this.applicationName=appName;
        return  this;
    }

    /**
     * 配置配置中心
     * @param registryConfig
     * @return
     */
    public PrpcBootstrap registry(RegistryConfig registryConfig) {
        this.registry=registryConfig.getRegistry();
        return this;
    }

    /**
     * 配置使用的协议
     * @param protocolConfig 协议的封装
     * @return this当前实例
     */
    public PrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig=protocolConfig;
        if (log.isDebugEnabled()){
            log.debug("当前工程使用了：{}协议进行序列化",protocolConfig.toString());
        }
        return this;
    }

    /**
     * 发布服务(注册到服务中心)
     * @param service 封装需要发布的服务
     * @return this当前实例
     */
    public PrpcBootstrap publish(ServiceConfig<?> service){
        //使用注册中心的实现完成注册
        registry.register(service);
        SERVICE_LIST.put(service.getInterface().getName(),service);
        return this;
    }

    /**
     * 批量发布
     * @param services
     * @return
     */
    public PrpcBootstrap publish(List<ServiceConfig<?>> services){
        for (ServiceConfig service:services){
            this.publish(service);
        }
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start(){
        EventLoopGroup boss=new NioEventLoopGroup(2);
        EventLoopGroup worker=new NioEventLoopGroup(10);
        try{
            //服务器引导程序
            ServerBootstrap serverBootstrap=new ServerBootstrap();
            //配置服务器
            serverBootstrap=serverBootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LoggingHandler())
                                    .addLast(new PrpcRequestDecoder())
                                    //根据请求进行方法调用
                                    .addLast(new MethodCallHandler())
                                    .addLast(new PrpcResponseEncoder());

                        }
                    });
            //绑定端口
            ChannelFuture channelFuture=serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //---------------------------------consume------------------------
    public PrpcBootstrap reference(ReferenceConfig<?> reference) {
        reference.setRegistry(registry);
        return this;
    }

    /**
     * 配置序列化方式
     * @param serializeType
     * @return
     */
    public PrpcBootstrap serialize(String serializeType) {
        SERIALIZE_TYPE=serializeType;
        if (log.isDebugEnabled()){
            log.debug("配置了使用序列化的方式【{}】",serializeType);
        }
        return this;
    }
    public PrpcBootstrap compress(String compressType) {
        COMPRESS_TYPE=compressType;
        if (log.isDebugEnabled()){
            log.debug("配置了使用压缩算法的方式【{}】",compressType);
        }
        return this;
    }
}

package com.czp;

import com.czp.annotation.PrpcApi;
import com.czp.channnelHandler.handler.MethodCallHandler;
import com.czp.channnelHandler.handler.PrpcRequestDecoder;
import com.czp.channnelHandler.handler.PrpcResponseEncoder;
import com.czp.config.Configuration;
import com.czp.core.HeartbeatDetector;
import com.czp.discovery.Registry;
import com.czp.hook.PrpcShutDownHook;
import com.czp.loadbalancer.LoadBalancer;
import com.czp.loadbalancer.impl.RoundRobinLoadBalancer;
import com.czp.protection.CircuitBreaker;
import com.czp.protection.RateLimiter;
import com.czp.transport.message.PrpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class PrpcBootstrap {


    //单例模式
    private static PrpcBootstrap prpcBootstrap=new PrpcBootstrap();

    //全局配置中心
    private final Configuration configuration;

    //连接的缓存
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE=new ConcurrentHashMap<>();
    public final static TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE=new TreeMap<>();

    public static final Map<String,ServiceConfig<?>> SERVICE_LIST=new HashMap<>(16);
    //定义全局的对外挂起的completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST=new ConcurrentHashMap<>();
    //全局的id生成器
    public static final ThreadLocal<PrpcRequest> REQUEST_THREAD_LOCAL=new ThreadLocal<>();

    //限流器和熔断器的缓存
    public static final Map<InetSocketAddress, RateLimiter> RATE_LIMITER_MAP=new ConcurrentHashMap<>();
    public static final Map<InetSocketAddress, CircuitBreaker> CIRCUIT_BREAKER_MAP=new ConcurrentHashMap<>();


    //构造器初始化配置
    private PrpcBootstrap(){
        configuration=new Configuration();
    }

    public Configuration getConfiguration() {
        return configuration;
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
        configuration.setApplicationName(appName);
        return  this;
    }

    /**
     * 配置配置中心
     * @param registryConfig
     * @return
     */
    public PrpcBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        return this;
    }


    /**
     * 配置负载均衡策略
     * @param loadBalancer
     * @return
     */
    public PrpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }



    /**
     * 发布服务(注册到服务中心)
     * @param service 封装需要发布的服务
     * @return this当前实例
     */
    public PrpcBootstrap publish(ServiceConfig<?> service){
        //使用注册中心的实现完成注册
        configuration.getRegistryConfig().getRegistry().register(service, service.getGroup());
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
        Runtime.getRuntime().addShutdownHook(new PrpcShutDownHook());
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
            ChannelFuture channelFuture=serverBootstrap.bind(configuration.getPort()).sync();
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
        log.debug("[{}]开始心跳检测",reference.getInterface().getName());
        //创建守护线程持续做心跳检测
        HeartbeatDetector.detectHeartbeat(reference.getInterface().getName());
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        reference.setGroup(this.getConfiguration().getGroup());
        return this;
    }

    /**
     * 配置序列化方式
     * @param serializeType
     * @return
     */
    public PrpcBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        if (log.isDebugEnabled()){
            log.debug("配置了使用序列化的方式【{}】",serializeType);
        }
        return this;
    }
    public PrpcBootstrap compress(String compressType) {
        configuration.setCompressType(compressType);
        if (log.isDebugEnabled()){
            log.debug("配置了使用压缩算法的方式【{}】",compressType);
        }
        return this;
    }
    public Registry getRegistry() {
        return configuration.getRegistryConfig().getRegistry();
    }

    public  PrpcBootstrap scan(String packageName){
        //通过packageName获取路径下所有的类的权限定名称
        List<String> classNames=getAllClassName(packageName);
        //通过反射获取类信息发布
        List<Class<?>> classes = classNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(PrpcApi.class) != null)
                .collect(Collectors.toList());
        for (Class<?> clazz:classes){
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance=null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            PrpcApi prpcApi = clazz.getAnnotation(PrpcApi.class);
            String group = prpcApi.group();

            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                serviceConfig.setGroup(group);
                if (log.isDebugEnabled()){
                    log.debug("通过包扫描将服务【{}】发布",anInterface);
                }
                publish(serviceConfig);
            }
        }
        return this;
    }

    private static List<String> getAllClassName(String packageName) {
        //com.czp......---->E://com/czp/.....
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (url == null) {
            throw new RuntimeException("包扫扫描时路径不存在");
        }
        String absolutePath = url.getPath();
        List<String > classnames=new ArrayList<>();
        classnames=recursionFile(absolutePath,classnames,basePath);
        return classnames;
    }

    private static List<String> recursionFile(String absolutePath, List<String> classnames, String basePath) {
        File file = new File(absolutePath);
        if (file.isDirectory()){
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (children==null||children.length==0){
                return classnames;
            }
            for (File child : children) {
                recursionFile(child.getAbsolutePath(),classnames,basePath);
            }
        }else{
            String className=getClassNameByAbsolutePath(absolutePath,basePath);
            classnames.add(className);
        }
        return classnames;
    }

    private static String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        //E:\project\prpc-core\target\classes\com\czp\serialize\Serializer.class--->com.czp.serialize.Serializer
        String fileName = absolutePath.substring(absolutePath.indexOf(basePath.replaceAll("/", "\\\\"))).
                replaceAll("\\\\", ".");
        fileName=fileName.substring(0,fileName.indexOf(".class"));
        return fileName;

    }
    public PrpcBootstrap  group(String group){
        this.getConfiguration().setGroup(group);
        return this;
    }

    public static void main(String[] args) {
        PrpcBootstrap.getInstance().scan("com.czp");
    }
}

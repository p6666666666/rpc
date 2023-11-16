package com.czp.watchers;

import com.czp.NettyBootstrapInitializer;
import com.czp.PrpcBootstrap;
import com.czp.discovery.Registry;
import com.czp.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

@Slf4j
public class UpAndDownWatcher implements Watcher {

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType()==Event.EventType.NodeChildrenChanged){
            if (log.isDebugEnabled()){
                log.debug("检测到有服务【{}】节点上/下线,将重新拉取服务列表",watchedEvent.getPath());
            }
            String serviceName=getServiceName(watchedEvent.getPath());
            Registry registry = PrpcBootstrap.getInstance().getRegistry();
            List<InetSocketAddress> addresses = registry.lookUpService(serviceName,PrpcBootstrap.getInstance().getConfiguration().getGroup());
            for (InetSocketAddress address : addresses) {
                //处理新增节点
                if (!PrpcBootstrap.CHANNEL_CACHE.containsKey(address)){
                    Channel channel=null;
                    try {
                        channel= NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    PrpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            }
            //处理上线节点
            for (Map.Entry<InetSocketAddress, Channel> entry : PrpcBootstrap.CHANNEL_CACHE.entrySet()) {
                if (!addresses.contains(entry.getKey())){
                    PrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }
            LoadBalancer loadBalancer = PrpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            loadBalancer.reLoadBalance(serviceName,addresses);

        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length-1];
    }
}

package com.czp.discovery.impl;

import com.czp.Constant;
import com.czp.ServiceConfig;
import com.czp.discovery.AbstractRegistry;
import com.czp.exception.NetWorkException;
import com.czp.utils.NetUtils;
import com.czp.utils.ZookeeperNode;
import com.czp.utils.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    private ZooKeeper zookeeper;
    public ZookeeperRegistry() {
        this.zookeeper = ZookeeperUtil.createZookeeper();
    }
    public ZookeeperRegistry(String connectString,int timeout){
        this.zookeeper=ZookeeperUtil.createZookeeper(connectString,timeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {
        //服务名称的节点
        String parentNode= Constant.BASE_PROVIDERS_PATH+"/"+service.getInterface().getName();
        //创建服务持久节点
        ZookeeperNode zookeeperNode=new ZookeeperNode(parentNode,null);
        ZookeeperUtil.createNode(zookeeper,zookeeperNode,null, CreateMode.PERSISTENT);
        //注册本机临时节点
        String epNodePath=parentNode+"/"+ NetUtils.getIp()+":"+8088;
        ZookeeperNode epNode = new ZookeeperNode(epNodePath,null);
        ZookeeperUtil.createNode(zookeeper,epNode,null, CreateMode.EPHEMERAL);


        if (log.isDebugEnabled()){
            log.debug("服务{}，已经被注册",service.getInterface().getName());
        }
    }

    @Override
    public InetSocketAddress lookUpService(String interfaceName) {
        //获取节点
        String nodePath=Constant.BASE_PROVIDERS_PATH+"/"+interfaceName;
        List<String> children = ZookeeperUtil.getChildren(zookeeper, nodePath, null);
        List<InetSocketAddress> collect = children.stream().map(ipString -> {
            String[] ipAndPort = ipString.split(":");
            String ip = ipAndPort[0];
            int port = Integer.valueOf(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).collect(Collectors.toList());
        if (collect.size()==0){
            throw new NetWorkException();
        }
        return collect.get(0);
    }
}

package com.czp;

import com.czp.utils.ZookeeperNode;
import com.czp.utils.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;


import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 注册中心管理
 */
@Slf4j
public class Application {
    public static void main(String[] args) {

        //创建zookeeper实例
        ZooKeeper zooKeeper= ZookeeperUtil.createZookeeper();
        //定义节点和数据
        String basePath="/prpc-metadata";
        ZookeeperNode baseNode = new ZookeeperNode("/prpc-metadata", null);
        ZookeeperNode provideNode = new ZookeeperNode(basePath+"/providers", null);
        ZookeeperNode consumerNode = new ZookeeperNode(basePath+"/consumers", null);
        //创建节点
        ZookeeperUtil.createNode(zooKeeper,baseNode,null,CreateMode.PERSISTENT);
        ZookeeperUtil.createNode(zooKeeper,provideNode,null,CreateMode.PERSISTENT);
        ZookeeperUtil.createNode(zooKeeper,consumerNode,null,CreateMode.PERSISTENT);
        //关闭zookeeper
        ZookeeperUtil.close(zooKeeper);


    }
}

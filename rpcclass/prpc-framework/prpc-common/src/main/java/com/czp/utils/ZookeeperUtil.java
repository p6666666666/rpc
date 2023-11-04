package com.czp.utils;

import com.czp.Constant;
import com.czp.exception.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZookeeperUtil {
    /**
     * 默认配置创建zookeeper连接
     * @return
     */
    public static ZooKeeper createZookeeper(){
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int timeout=Constant.DEFAULT_ZK_CONNECT_TIME_OUT;
        return createZookeeper(connectString,timeout);
    }
    public static ZooKeeper createZookeeper(String connectString,int timeout){
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try{
            //创建zookeeper实例
            ZooKeeper zooKeeper=new ZooKeeper(connectString,timeout, event -> {
                if (event.getState()== Watcher.Event.KeeperState.SyncConnected){
                    log.debug("zookeeper客户端链接成功");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zooKeeper;
        }catch (IOException  |InterruptedException e){
            log.debug("zookeeper链接异常",e);
            throw new ZookeeperException();
        }
    }

    /**
     * 创建一个zookeeper节点
     * @param zooKeeper
     * @param zookeeperNode
     * @param watcher
     * @param createMode
     * @return ture:成功创建 false:创建失败
     */
    public static boolean createNode(ZooKeeper zooKeeper,ZookeeperNode zookeeperNode,Watcher watcher,CreateMode createMode) {
        try{
            if (zooKeeper.exists(zookeeperNode.getNodePath(),watcher)==null){
                zooKeeper.create(zookeeperNode.getNodePath(), null,
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.debug(zookeeperNode.getNodePath()+"节点被创建");
                return true;
            }else {
                if (log.isDebugEnabled()){
                    log.info("节点{}已经存在",zookeeperNode.getNodePath());
                }
                return false;
            }
        }catch (KeeperException|InterruptedException e){
            log.debug("创建节点异常",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断节点是否存在
     * @param zk
     * @param node
     * @param watcher
     * @return true 存在 false不存在
     */
    public static boolean exists(ZooKeeper zk,String node,Watcher watcher){
        try {
            return zk.exists(node,watcher)!=null;
        } catch (KeeperException |InterruptedException e) {
            log.error("判断节点{}是否存在发生异常",node,e);
            throw new ZookeeperException(e);
        }
    }

    /**
     * 关闭zookeeper
     * @param zooKeeper
     */
    public static void close(ZooKeeper zooKeeper) {
        try {
            zooKeeper.close();
        }catch (InterruptedException e){
            log.error("关闭zookeeper发生问题",e);
            throw new ZookeeperException();
        }
    }

    public static List<String> getChildren(ZooKeeper zooKeeper, String nodePath, Watcher watcher) {
        try {
            return zooKeeper.getChildren(nodePath, watcher);
        } catch (KeeperException |InterruptedException e) {
            log.error("获取zk结点子元素失败",nodePath);
            e.printStackTrace();
            throw new ZookeeperException(e);
        }
    }
}

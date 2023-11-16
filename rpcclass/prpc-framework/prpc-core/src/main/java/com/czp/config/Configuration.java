package com.czp.config;

import com.czp.IdGenerator;
import com.czp.ProtocolConfig;
import com.czp.RegistryConfig;
import com.czp.discovery.Registry;
import com.czp.loadbalancer.LoadBalancer;
import com.czp.loadbalancer.impl.RoundRobinLoadBalancer;
import com.sun.rowset.internal.XmlResolver;
import lombok.Data;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLResolver;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;

@Data
public class Configuration {
    //端口号配置
    private int port =8092 ;
    //应用程序名字
    private String applicationName="default";
    //分组信息
    private String group="default";
    //注册中心配置
    private RegistryConfig registryConfig=new RegistryConfig("zookeeper://120.27.214.4:2181");

    //序列化协议
    private String serializeType="jdk";

    //压缩方式
    private  String compressType = "gzip";

    //id生成器
    public IdGenerator idGenerator=new IdGenerator(1,2);

    //负载均衡策略
    private LoadBalancer loadBalancer=new RoundRobinLoadBalancer();

    public Configuration() {

        //读取spi信息
        SpiRead spiRead=new SpiRead();
        spiRead.loadFromSpi(this);
        //获取xml信息
        XmlRead xmlRead=new XmlRead();
        xmlRead.loadFromXml(this);

    }

}

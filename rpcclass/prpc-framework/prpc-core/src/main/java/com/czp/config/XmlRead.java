package com.czp.config;

import com.czp.IdGenerator;
import com.czp.RegistryConfig;
import com.czp.loadbalancer.LoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

@Slf4j
public class XmlRead {
    public void loadFromXml(Configuration configuration){
        try{
            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            //禁止dtd验证
            factory.setValidating(false);
            //禁止外部实体解析
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("prpc.xml");
            Document doc = documentBuilder.parse(inputStream);

            //xpath解析器
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            //解析标签
            configuration.setPort(resolvePort(doc,xPath));
            configuration.setApplicationName(resolveAppname(doc,xPath));
            configuration.setRegistryConfig(resolveRegistry(doc,xPath));
            configuration.setSerializeType(resolveSerializeType(doc,xPath));
            configuration.setCompressType(resolveCompressType(doc,xPath));
            configuration.setLoadBalancer(resolveLoadBalancer(doc,xPath));
            configuration.setIdGenerator(resolveIdGenerator(doc,xPath));

        }catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e){
            log.info("解析xml错误，将使用其他配置");
        }
    }

    private IdGenerator resolveIdGenerator(Document doc, XPath xPath) throws XPathExpressionException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String expression = "/configuration/idGenerator";
        String aClass = parseString(doc, xPath, expression, "class");
        String dataCenterId = parseString(doc, xPath, expression, "dataCenterId");
        String machineId = parseString(doc, xPath, expression, "machineId");
        Class<?> clazz = Class.forName(aClass);
        Object instance = clazz.getConstructor(new Class[]{long.class, long.class})
                .newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
        return (IdGenerator) instance;
    }

    private LoadBalancer resolveLoadBalancer(Document doc, XPath xPath) throws XPathExpressionException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String expression="/configuration/loadBalancer";
        LoadBalancer loadBalancer = parseObject(doc, xPath, expression, null);
        return loadBalancer;
    }



    private String resolveCompressType(Document doc, XPath xPath) throws XPathExpressionException {
        String expression="/configuration/compressType";
        String type = parseString(doc, xPath, expression, "type");
        return type;
    }

    private String resolveSerializeType(Document doc, XPath xPath) throws XPathExpressionException {
        String expression="/configuration/serializeType";
        String type = parseString(doc, xPath, expression, "type");
        return type;
    }

    private RegistryConfig resolveRegistry(Document doc, XPath xPath) throws XPathExpressionException {
        String expression="/configuration/registry";
        String url=parseString(doc,xPath,expression,"url");
        return new RegistryConfig(url);
    }



    private String resolveAppname(Document doc, XPath xPath) throws XPathExpressionException {
        String expression="/configuration/appName";
        return parseString(doc,xPath,expression);
    }

    private int resolvePort(Document doc, XPath xPath) throws XPathExpressionException {
        String expression="/configuration/port";
        String port=parseString(doc,xPath,expression);
        return Integer.parseInt(port);
    }

    private String parseString(Document doc, XPath xPath, String expression) throws XPathExpressionException {
        XPathExpression compile = xPath.compile(expression);
        Node targetNode= (Node)compile.evaluate(doc, XPathConstants.NODE);
        return targetNode.getTextContent();
    }
    private String parseString(Document doc, XPath xPath, String expression, String attribute) throws XPathExpressionException {
        XPathExpression compile = xPath.compile(expression);
        Node targetNode= (Node)compile.evaluate(doc, XPathConstants.NODE);
        String nodeValue = targetNode.getAttributes().getNamedItem(attribute).getNodeValue();
        return nodeValue;
    }
    private <T> T parseObject(Document doc, XPath xPath, String expression, Class<?>[] paramType,Object... param) throws XPathExpressionException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String aClass = parseString(doc, xPath, expression, "class");
        Class<?> clazz = Class.forName(aClass);
        Object instant=null;
        if (paramType==null){
            instant= clazz.getConstructor().newInstance();
        }else {
            instant = clazz.getConstructor(paramType).newInstance(param);
        }
        return (T)instant;
    }

}

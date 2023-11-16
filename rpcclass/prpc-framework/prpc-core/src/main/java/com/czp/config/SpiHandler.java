package com.czp.config;

import com.czp.compress.Compressor;
import com.czp.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class SpiHandler {
    private static final String BASE_PATH="META-INF/prpc-services";


    //缓存spi文件下的内容
    private static final Map<String, List<String>> SPI_CONTENT=new ConcurrentHashMap<>(8);

    //缓存实例
    private static final Map<Class<?>,List<ObjectWrapper<?>>> SPI_IMPLEMENT = new ConcurrentHashMap<>(32);

    //读取spi路径下的文件
    static{
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(BASE_PATH);
        if (url!=null){
            File file = new File(url.getPath());
            File[] files = file.listFiles();
            if (files!=null){
                for (File child : files) {
                    String name=child.getName();
                    List<String> implNames = getImplNames(child);
                    SPI_CONTENT.put(name,implNames);
                }
            }
        }
    }

    public synchronized  static <T> ObjectWrapper<T> get(Class<T> clazz){
        //先走缓存
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if (objectWrappers!=null||objectWrappers.size()>0){
            return (ObjectWrapper<T>)objectWrappers.get(0);
        }
        //构建缓存
        buildCache(clazz);

        //重新获取实例
        List<ObjectWrapper<?>>  result = SPI_IMPLEMENT.get(clazz);
        if (result == null || result.size() == 0){
            return null;
        }
        return (ObjectWrapper<T>) result.get(0);
    }
    public synchronized static <T> List<ObjectWrapper<T>> getList(Class<T> clazz) {

        // 走缓存
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && objectWrappers.size() > 0){
            return objectWrappers.stream().map( wrapper -> (ObjectWrapper<T>)wrapper )
                    .collect(Collectors.toList());
        }

        // 构建缓存
        buildCache(clazz);

        // 再次获取
        objectWrappers = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && objectWrappers.size() > 0){
            return objectWrappers.stream().map( wrapper -> (ObjectWrapper<T>)wrapper )
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private static <T> void buildCache(Class<T> clazz) {
        String name = clazz.getName();
        List<String> strings = SPI_CONTENT.get(name);
        if (strings==null ||strings.size()==0){
            return;
        }
        List<ObjectWrapper<?>> list=new ArrayList<>();
        for (String string : strings) {
            String[] spiString = string.split("-");
            if (spiString.length!=3){
                return;
            }
            Byte code=Byte.valueOf(spiString[0]);
            String type=spiString[1];
            String implPath=spiString[2];
            Object object = null;
            try {
                Class<?> aClass = Class.forName(implPath);
                object = aClass.getConstructor().newInstance();
                ObjectWrapper<?> objectWrapper = new ObjectWrapper<>(code, type,object);
                list.add(objectWrapper);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("实例化spi[{}]对象时发生异常",clazz);
                System.out.println(e);
            }
        }
        SPI_IMPLEMENT.put(clazz,list);
    }


    private static List<String> getImplNames(File child) {
        List<String> list=new ArrayList<>();
        try(
                FileReader fileReader=new FileReader(child);
                BufferedReader bufferedReader=new BufferedReader(fileReader)
                ){
            while(true){
                String line = bufferedReader.readLine();
                if (line==null||line.length()==0){
                    break;
                }
                list.add(line);
            }
            return list;

        }catch (IOException e){
            log.error("读取文件时发生异常");
        }
        return null;
    }


    public static void main(String[] args) {
        SpiHandler spiHandler=new SpiHandler();
        SpiHandler.getList(Serializer.class);
    }
}

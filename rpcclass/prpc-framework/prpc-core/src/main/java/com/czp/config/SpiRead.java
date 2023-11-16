package com.czp.config;

import com.czp.compress.CompressFactory;
import com.czp.compress.Compressor;
import com.czp.loadbalancer.LoadBalancer;
import com.czp.serializer.Serializer;
import com.czp.serializer.SerializerFactory;

import java.util.List;

public class SpiRead {

    public void loadFromSpi(Configuration configuration) {
        List<ObjectWrapper<LoadBalancer>> loadBalancers = SpiHandler.getList(LoadBalancer.class);
        if (!loadBalancers.isEmpty()){
            configuration.setLoadBalancer(loadBalancers.get(0).getImpl());
        }
        List<ObjectWrapper<Serializer>> serializers = SpiHandler.getList(Serializer.class);
        if (!serializers.isEmpty()){
            serializers.forEach(SerializerFactory::addSerializer);
        }
        List<ObjectWrapper<Compressor>> compressors = SpiHandler.getList(Compressor.class);
        if (!compressors.isEmpty()){
            compressors.forEach(CompressFactory::addCompressor);
        }



    }
}

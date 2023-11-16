package com.czp.impl;

import com.czp.HelloRpc;
import com.czp.annotation.PrpcApi;

@PrpcApi
public class HelloRpcImpl implements HelloRpc {
    @Override
    public String sayHi(String msg) {
        System.out.println(msg);
        return msg;
    }
}

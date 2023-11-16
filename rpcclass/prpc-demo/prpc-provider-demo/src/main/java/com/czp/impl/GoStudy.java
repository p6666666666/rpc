package com.czp.impl;

import com.czp.Study;
import com.czp.annotation.PrpcApi;

@PrpcApi(group = "main")
public class GoStudy implements Study {
    @Override
    public String sayHi(String msg) {
        return "哈哈--》study";
    }
}

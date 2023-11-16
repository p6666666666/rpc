package com.czp;

import com.czp.annotation.PrpcApi;
import com.czp.annotation.TryCount;

public interface HelloRpc {
    @TryCount(tryCount = 5,intervalTime = 2000)
    public String sayHi(String msg);
}

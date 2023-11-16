package com.czp.hook;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

public class ShutDownHolder {

    //标记拦截板
    public  static AtomicBoolean BAFFLE=new AtomicBoolean(false);

    //未完成请求的计数器
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}

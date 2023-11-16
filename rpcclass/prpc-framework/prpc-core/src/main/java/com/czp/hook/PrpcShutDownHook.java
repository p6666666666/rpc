package com.czp.hook;



public class PrpcShutDownHook extends Thread{
    @Override
    public void run() {
        //拦截请求
        ShutDownHolder.BAFFLE.set(true);


        //等待计数器归零或处理请求超过10秒
        long start = System.currentTimeMillis();
        while(true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (System.currentTimeMillis()-start>1000||ShutDownHolder.REQUEST_COUNTER.sum()==0L){
                break;
            }
        }
    }
}

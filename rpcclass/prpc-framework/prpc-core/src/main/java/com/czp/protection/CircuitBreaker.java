package com.czp.protection;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {

    //熔断器的状态
    private volatile boolean openState=false;

    //记录总的请求数
    private AtomicInteger requestCount=new AtomicInteger(0);

    //记录错误的请求数
    private AtomicInteger requestErrorCount=new AtomicInteger(0);

    //异常值标准
    private int maxErrorCount;
    private float maxErrorCountRate;

    public CircuitBreaker(int maxErrorCount, float maxErrorCountRate) {
        this.maxErrorCount = maxErrorCount;
        this.maxErrorCountRate = maxErrorCountRate;
    }

    public boolean isBreak(){
        if (openState){
            return true;
        }
        if (requestErrorCount.get()>maxErrorCount){
            openState=true;
            return true;
        }
        if (requestErrorCount.get()/(float)requestCount.get()>maxErrorCountRate

        &&requestCount.get()>0&&requestErrorCount.get()>0){
            openState=true;
            return true;
        }
        return false;

    }
    public void recordRequest(){
        this.requestCount.getAndIncrement();
    }

    public void recordErrorRequest(){
        this.requestErrorCount.getAndIncrement();
    }

    //重置熔断器
    public void reset(){
        this.openState = false;
        this.requestCount.set(0);
        this.requestErrorCount.set(0);
    }
    public static void main(String[] args) {

        CircuitBreaker circuitBreaker = new CircuitBreaker(3,1.1F);

        new Thread(() ->{
            for (int i = 0; i < 1000; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                circuitBreaker.recordRequest();
                int num = new Random().nextInt(100);
                if(num > 70){
                    circuitBreaker.recordErrorRequest();
                }

                boolean aBreak = circuitBreaker.isBreak();

                String result = aBreak ? "断路器阻塞了请求":"断路器放行了请求";

                System.out.println(result);

            }
        }).start();


        new Thread(() -> {
            for (;;) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("-----------------------------------------");
                circuitBreaker.reset();
            }
        }).start();

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}

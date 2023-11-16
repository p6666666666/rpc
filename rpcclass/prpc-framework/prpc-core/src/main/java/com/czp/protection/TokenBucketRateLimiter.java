package com.czp.protection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenBucketRateLimiter implements RateLimiter{

    //令牌数量
    private int tokens;
    //令牌桶的容量
    private  int capacity;

    //每秒放的令牌数量
    private  int rate;

    //上次拿走令牌的时间
    private Long lastTokenTime;

    public TokenBucketRateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        this.lastTokenTime = System.currentTimeMillis();
        tokens=capacity;
    }

    @Override
    public boolean allowRequest() {
        long intervalTime=System.currentTimeMillis()-lastTokenTime;
        if (intervalTime>1000){
            int addTokens= (int) (intervalTime*rate/1000);
            tokens=Math.min(tokens+addTokens,capacity);
            this.lastTokenTime=System.currentTimeMillis();
        }
        if (tokens>0){
            tokens--;
            log.info("请求成功，没有被限流——————");
            return true;
        }else {
            log.info("请求被限流————————");
            return false;
        }

    }
    public static void main(String[] args) {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(100,10);
        for (int i = 0; i < 1000; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            boolean allowRequest = rateLimiter.allowRequest();
            System.out.println("allowRequest = " + allowRequest);
        }
    }
}

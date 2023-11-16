package com.czp.protection;

public interface RateLimiter {
    boolean allowRequest();
}

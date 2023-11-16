package com.czp.exception;

public class LoadBalancerException extends RuntimeException {
    public LoadBalancerException() {
    }

    public LoadBalancerException(Throwable cause) {
        super(cause);
    }

    public LoadBalancerException(String message) {
        super(message);
    }
}

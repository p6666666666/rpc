package com.czp.exception;

public class CompressException extends RuntimeException {
    public CompressException() {
    }

    public CompressException(Throwable cause) {
        super(cause);
    }

    public CompressException(String message) {
        super(message);
    }
}

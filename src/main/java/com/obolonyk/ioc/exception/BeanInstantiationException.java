package com.obolonyk.ioc.exception;

public class BeanInstantiationException extends RuntimeException {

    public BeanInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}

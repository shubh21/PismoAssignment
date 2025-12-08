package com.takeHome.Pismo.core.exception;

public abstract class DomainException extends RuntimeException{
    public DomainException(String message) {
        super(message);
    }
}

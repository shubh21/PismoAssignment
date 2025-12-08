package com.takeHome.Pismo.core.exception;

public class AccountNotFoundException extends DomainException{

    public AccountNotFoundException(String message) {
        super(message);
    }
}

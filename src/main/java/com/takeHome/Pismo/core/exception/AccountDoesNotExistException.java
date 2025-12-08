package com.takeHome.Pismo.core.exception;

public class AccountDoesNotExistException extends DomainException{

    public AccountDoesNotExistException(String message){
        super(message);
    }
}

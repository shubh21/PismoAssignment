package com.takeHome.Pismo.core;

public final class Constants {

    private Constants(){
    }

    public static final String INVALID_DOCUMENT_NUMBER_MSG = "Invalid Document Number - %s";
    public static final String INVALID_ACCOUNT_ID_MSG = "Invalid Account Id - %s";
    public static final String INVALID_TRANSACTION_ID_MSG = "Invalid Transaction Id - %s";
    public static final String INVALID_OPERATION_ID_MSG = "Invalid operation type id - %s";
    public static final String INVALID_AMOUNT_VALUE_MSG = "Invalid Amount value - %s";
    public static final String ACCOUNT_NOT_FOUND_EXCEPTION_MSG = "Account Not Found for account_id - %s";
    public static final String DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG = "Account with document number %s already exists";
    public static final String ACCOUNT_NOT_EXISTS_EXCEPTION_MSG = "Account Id %s does not exist";
    public static final String KEY_GENERATION_ERROR_MSG = "Failed to retrieve generated %s id";
}

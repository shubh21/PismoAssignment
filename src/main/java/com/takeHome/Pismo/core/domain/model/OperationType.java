package com.takeHome.Pismo.core.domain.model;

import com.takeHome.Pismo.core.Constants;

public enum OperationType {
    CASH_PURCHASE(1),
    INSTALLMENT_PURCHASE(2),
    WITHDRAWAL(3),
    PAYMENT(4);

    private final int id;

    public int getId() {
        return id;
    }

    OperationType(int id){
        this.id = id;
    }

    public static OperationType fromId(int id){
        for(OperationType type:values()){
            if(type.id == id){
                return type;
            }
        }
        throw new IllegalArgumentException(String.format(Constants.INVALID_OPERATION_ID_MSG, id));
    }
}

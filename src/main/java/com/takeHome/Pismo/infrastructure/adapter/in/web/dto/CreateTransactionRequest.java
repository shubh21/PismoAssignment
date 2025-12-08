package com.takeHome.Pismo.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

import static com.takeHome.Pismo.core.Constants.INVALID_ACCOUNT_ID_MSG;
import static com.takeHome.Pismo.core.Constants.INVALID_AMOUNT_VALUE_MSG;

public record CreateTransactionRequest(@JsonProperty("account_id")  long accountId,
                                       @JsonProperty("operation_type_id")  int operationTypeId,
                                       BigDecimal amount) {
    public CreateTransactionRequest{
        if(accountId <= 0L){
            throw new IllegalArgumentException(INVALID_ACCOUNT_ID_MSG.formatted(accountId));
        }
        if(Objects.isNull(amount) || amount.compareTo(BigDecimal.ZERO)<0){
            throw new IllegalArgumentException(INVALID_AMOUNT_VALUE_MSG.formatted(amount));
        }
    }
}

package com.takeHome.Pismo.infrastructure.adapter.in.web;

import com.takeHome.Pismo.core.contract.output.AccountResult;
import com.takeHome.Pismo.core.contract.output.TransactionResult;
import com.takeHome.Pismo.infrastructure.adapter.in.web.dto.AccountResponse;
import com.takeHome.Pismo.infrastructure.adapter.in.web.dto.TransactionResponse;

public class WebResponseMapper {

    public static AccountResponse toAccountResponse(AccountResult result){

        return AccountResponse.builder()
                .accountId(result.accountId())
                .documentNumber(result.documentNumber())
                .build();
    }

    public static TransactionResponse toTransactionResponse(TransactionResult result){

        return TransactionResponse.builder()
                .transactionId(result.transactionId())
                .accountId(result.accountId())
                .amount(result.amount())
                .operationTypeId(result.operationTypeId())
                .eventDate(result.eventDate())
                .build();
    }
}

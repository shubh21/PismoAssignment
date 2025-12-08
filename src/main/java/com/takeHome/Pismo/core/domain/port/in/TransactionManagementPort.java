package com.takeHome.Pismo.core.domain.port.in;


import com.takeHome.Pismo.core.contract.input.CreateTransactionCommand;
import com.takeHome.Pismo.core.contract.output.TransactionResult;

public interface TransactionManagementPort {
    TransactionResult saveTransaction(CreateTransactionCommand createTransactionCommand);
}

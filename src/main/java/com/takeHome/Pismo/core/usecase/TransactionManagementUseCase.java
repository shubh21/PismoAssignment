package com.takeHome.Pismo.core.usecase;

import com.takeHome.Pismo.core.contract.input.CreateTransactionCommand;
import com.takeHome.Pismo.core.contract.output.TransactionResult;
import com.takeHome.Pismo.core.domain.model.OperationType;
import com.takeHome.Pismo.core.domain.model.Transaction;
import com.takeHome.Pismo.core.domain.port.in.TransactionManagementPort;
import com.takeHome.Pismo.core.domain.port.out.TransactionPersistencePort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionManagementUseCase implements TransactionManagementPort {

    private final TransactionPersistencePort transactionPersistencePort;

    public TransactionManagementUseCase(TransactionPersistencePort transactionPersistencePort) {
        this.transactionPersistencePort = transactionPersistencePort;
    }

    @Override
    public TransactionResult saveTransaction(CreateTransactionCommand createTransactionCommand) {

        Transaction transaction = Transaction.builder()
                .accountId(createTransactionCommand.accountId())
                .operationTypeId(createTransactionCommand.operationType().getId())
                .amount(applySignRule(createTransactionCommand.operationType(), createTransactionCommand.amount()))
                .eventDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionPersistencePort.save(transaction);

        return mapToTransactionResult(savedTransaction);
    }

    private BigDecimal applySignRule(OperationType operationType, BigDecimal amount) {

        return switch (operationType) {
            case CASH_PURCHASE,WITHDRAWAL, INSTALLMENT_PURCHASE ->
                    amount.compareTo(BigDecimal.ZERO)>0 ? amount.negate():amount;
            case PAYMENT -> amount.compareTo(BigDecimal.ZERO)<0 ? amount.negate() : amount;
        };
    }


    private TransactionResult mapToTransactionResult(Transaction transaction) {
        return TransactionResult.builder()
                .transactionId(transaction.transactionId())
                .accountId(transaction.accountId())
                .operationTypeId(transaction.operationTypeId())
                .amount(transaction.amount())
                .eventDate(transaction.eventDate()).build();
    }
}

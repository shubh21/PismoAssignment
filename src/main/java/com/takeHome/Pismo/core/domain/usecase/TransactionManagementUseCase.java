package com.takeHome.Pismo.core.domain.usecase;

import com.takeHome.Pismo.core.contract.input.CreateTransactionCommand;
import com.takeHome.Pismo.core.contract.output.TransactionResult;
import com.takeHome.Pismo.core.domain.model.Transaction;
import com.takeHome.Pismo.core.domain.port.in.TransactionManagementPort;
import com.takeHome.Pismo.core.domain.port.out.TransactionPersistencePort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static com.takeHome.Pismo.core.Constants.INVALID_OPERATION_ID_MSG;

public class TransactionManagementUseCase implements TransactionManagementPort {

    private final TransactionPersistencePort transactionPersistencePort;

    public TransactionManagementUseCase(TransactionPersistencePort transactionPersistencePort) {
        this.transactionPersistencePort = transactionPersistencePort;
    }

    @Override
    public TransactionResult saveTransaction(CreateTransactionCommand createTransactionCommand) {
        int opTypeId = createTransactionCommand.operationType().getId();

        Transaction transaction = Transaction.builder()
                .accountId(createTransactionCommand.accountId())
                .operationTypeId(opTypeId)
                .amount(applySignRule(opTypeId, createTransactionCommand.amount()))
                .eventDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionPersistencePort.save(transaction);

        return mapToTransactionResult(savedTransaction);
    }

    private BigDecimal applySignRule(int operationTypeId, BigDecimal amount) {
        BigDecimal formattedAmount = amount.setScale(2, RoundingMode.HALF_UP);

        return switch (operationTypeId) {
            case 1, 2, 3 -> formattedAmount.compareTo(BigDecimal.ZERO)>0 ? formattedAmount.negate():formattedAmount; //purchase and withdrawal
            case 4 -> formattedAmount.compareTo(BigDecimal.ZERO)<0 ? formattedAmount.negate() : formattedAmount;    // PAYMENT
            default -> throw new IllegalArgumentException(INVALID_OPERATION_ID_MSG.formatted(operationTypeId));
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

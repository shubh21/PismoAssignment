package com.takeHome.Pismo.core.usecase;

import com.takeHome.Pismo.core.contract.input.CreateTransactionCommand;
import com.takeHome.Pismo.core.contract.output.TransactionResult;
import com.takeHome.Pismo.core.domain.model.BalanceBearingTransaction;
import com.takeHome.Pismo.core.domain.model.ExecutableTransaction;
import com.takeHome.Pismo.core.domain.model.OperationType;
import com.takeHome.Pismo.core.domain.model.Transaction;
import com.takeHome.Pismo.core.domain.model.TransactionDischargeResult;
import com.takeHome.Pismo.core.domain.port.in.BalanceDischargePort;
import com.takeHome.Pismo.core.domain.port.in.TransactionManagementPort;
import com.takeHome.Pismo.core.domain.port.out.TransactionPersistencePort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class TransactionManagementUseCase implements TransactionManagementPort {

    private final TransactionPersistencePort transactionPersistencePort;
    private final BalanceDischargePort balanceDischargePort;

    public TransactionManagementUseCase(TransactionPersistencePort transactionPersistencePort, BalanceDischargePort balanceDischargePort) {
        this.transactionPersistencePort = transactionPersistencePort;
        this.balanceDischargePort = balanceDischargePort;
    }

    @Override
    @Transactional
    public TransactionResult saveTransactionAndDischargeDebitBalances(CreateTransactionCommand createTransactionCommand) {

        Transaction transaction = Transaction.builder()
                .accountId(createTransactionCommand.accountId())
                .operationTypeId(createTransactionCommand.operationType().getId())
                .amount(createTransactionCommand.amount())
                .eventDate(LocalDateTime.now())
                .build();

        BalanceBearingTransaction executableTransaction = ExecutableTransaction.from(transaction);

        BalanceBearingTransaction saved = transactionPersistencePort.save(executableTransaction);

        if(createTransactionCommand.operationType().equals(OperationType.PAYMENT)) {

            List<BalanceBearingTransaction> openDebits =
                    transactionPersistencePort.fetchDebitTransactions(createTransactionCommand.accountId());
            if(!openDebits.isEmpty()) {
                TransactionDischargeResult dischargeResult = balanceDischargePort.discharge(saved, openDebits);
                if (!Objects.isNull(dischargeResult)) {
                    dischargeResult.updatedDebits().forEach(transactionPersistencePort::updateTransaction);
                    transactionPersistencePort.updateTransaction(dischargeResult.payment());
                }
            }
        }
        return mapToTransactionResult(saved.transaction());
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

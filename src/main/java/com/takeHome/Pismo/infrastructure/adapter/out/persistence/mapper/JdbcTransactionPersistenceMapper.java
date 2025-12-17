package com.takeHome.Pismo.infrastructure.adapter.out.persistence.mapper;

import com.takeHome.Pismo.core.domain.model.BalanceBearingTransaction;
import com.takeHome.Pismo.core.domain.model.ExecutableTransaction;
import com.takeHome.Pismo.core.domain.model.Transaction;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.entity.TransactionEntity;

public class JdbcTransactionPersistenceMapper implements TransactionPersistenceMapper{
    @Override
    public TransactionEntity toEntity(BalanceBearingTransaction executable) {
        Transaction tx = executable.transaction();

        return new TransactionEntity(
                tx.transactionId(),
                tx.accountId(),
                tx.operationTypeId(),
                tx.amount(),
                executable.balance(),
                tx.eventDate()
        );
    }

    @Override
    public BalanceBearingTransaction toTransaction(TransactionEntity entity) {

        Transaction tx = new Transaction(entity.transactionId(), entity.accountId(), entity.operationTypeId(),
                entity.amount(), entity.eventDate());

        return ExecutableTransaction.from(tx, entity.balance());
    }
}

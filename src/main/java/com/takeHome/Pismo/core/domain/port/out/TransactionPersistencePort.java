package com.takeHome.Pismo.core.domain.port.out;

import com.takeHome.Pismo.core.domain.model.BalanceBearingTransaction;

import java.util.List;

public interface TransactionPersistencePort {
    BalanceBearingTransaction save(BalanceBearingTransaction transaction);
    List<BalanceBearingTransaction> fetchDebitTransactions(long accountId);
    void updateTransaction(BalanceBearingTransaction transaction);
}

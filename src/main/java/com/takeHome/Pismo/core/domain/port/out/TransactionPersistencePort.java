package com.takeHome.Pismo.core.domain.port.out;

import com.takeHome.Pismo.core.domain.model.Transaction;

public interface TransactionPersistencePort {
    Transaction save(Transaction transaction);
}

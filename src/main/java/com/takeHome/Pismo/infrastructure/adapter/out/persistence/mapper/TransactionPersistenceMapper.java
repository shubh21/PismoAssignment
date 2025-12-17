package com.takeHome.Pismo.infrastructure.adapter.out.persistence.mapper;

import com.takeHome.Pismo.core.domain.model.BalanceBearingTransaction;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.entity.TransactionEntity;

public interface TransactionPersistenceMapper {

    TransactionEntity toEntity(BalanceBearingTransaction executable);

    BalanceBearingTransaction toTransaction(TransactionEntity entity);
}

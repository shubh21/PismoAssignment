package com.takeHome.Pismo.infrastructure.adapter.out.persistence;

import com.takeHome.Pismo.core.domain.model.BalanceBearingTransaction;
import com.takeHome.Pismo.core.domain.model.ExecutableTransaction;
import com.takeHome.Pismo.core.domain.model.OperationType;
import com.takeHome.Pismo.core.domain.model.Transaction;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.entity.TransactionEntity;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.mapper.JdbcTransactionPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

public class JdbcTransactionPersistenceMapperTest {

    private JdbcTransactionPersistenceMapper jdbcTransactionPersistenceMapper;

    @BeforeEach
    void setup(){
        jdbcTransactionPersistenceMapper = new JdbcTransactionPersistenceMapper();
    }

    @Test
    void givenExecutableTransaction_whenToEntityCalled_thenFieldsAreMapped() {
        ExecutableTransaction executableTransaction = ExecutableTransaction
                .from(Transaction.builder()
                        .accountId(1L)
                        .amount(BigDecimal.TEN)
                        .operationTypeId(OperationType.PAYMENT.getId())
                        .build());

        TransactionEntity entity = jdbcTransactionPersistenceMapper.toEntity(executableTransaction);
        assertThat(entity.accountId()).isEqualTo(executableTransaction.transaction().accountId());
        assertThat(entity.operationTypeId()).isEqualTo(executableTransaction.transaction().operationTypeId());
        assertThat(entity.balance()).isEqualByComparingTo(executableTransaction.balance());
    }

    @Test
    public void givenTransactionEntity_whenToTransactionCalled_thenFieldsAreMapped() {
        TransactionEntity entity = new TransactionEntity(1L, 1L, 3,
                BigDecimal.TEN, BigDecimal.valueOf(-3), LocalDateTime.now());

        BalanceBearingTransaction executableTransaction = jdbcTransactionPersistenceMapper.toTransaction(entity);

        assertThat(entity.accountId()).isEqualTo(executableTransaction.transaction().accountId());
        assertThat(entity.operationTypeId()).isEqualTo(executableTransaction.transaction().operationTypeId());
        assertThat(executableTransaction.balance()).isEqualByComparingTo("-3");
    }
}

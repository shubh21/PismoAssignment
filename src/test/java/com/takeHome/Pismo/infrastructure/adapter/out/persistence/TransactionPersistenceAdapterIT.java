package com.takeHome.Pismo.infrastructure.adapter.out.persistence;

import com.takeHome.Pismo.core.Constants;
import com.takeHome.Pismo.core.domain.model.Account;
import com.takeHome.Pismo.core.domain.model.BalanceBearingTransaction;
import com.takeHome.Pismo.core.domain.model.ExecutableTransaction;
import com.takeHome.Pismo.core.domain.model.OperationType;
import com.takeHome.Pismo.core.domain.model.Transaction;
import com.takeHome.Pismo.core.exception.AccountDoesNotExistException;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.entity.TransactionEntity;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.mapper.JdbcTransactionPersistenceMapper;
import com.takeHome.Pismo.infrastructure.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static com.takeHome.Pismo.core.domain.model.OperationType.CASH_PURCHASE;
import static com.takeHome.Pismo.core.domain.model.OperationType.INSTALLMENT_PURCHASE;
import static com.takeHome.Pismo.core.domain.model.OperationType.PAYMENT;
import static com.takeHome.Pismo.core.domain.model.OperationType.WITHDRAWAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "spring.sql.init.mode=always"
})
public class TransactionPersistenceAdapterIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private AccountPersistenceAdapter accountPersistenceAdapter;

    private TransactionPersistenceAdapter transactionPersistenceAdapter;

    private static final  RowMapper<TransactionEntity> TRANSACTION_ROW_MAPPER = (rs, rowNum) ->
            new TransactionEntity(rs.getLong("TRANSACTION_ID"),
                    rs.getLong("ACCOUNT_ID"),
                    rs.getInt("OPERATIONTYPE_ID"),
                    rs.getBigDecimal("AMOUNT"),
                    rs.getBigDecimal("BALANCE"),
                    convertToLocalDate(rs.getTimestamp("EVENT_DATE")));


    @BeforeEach
    void setup(){
        TransactionPersistenceMapper transactionPersistenceMapper = new JdbcTransactionPersistenceMapper();
        transactionPersistenceAdapter = new TransactionPersistenceAdapter(jdbcTemplate, transactionPersistenceMapper);
        accountPersistenceAdapter = new AccountPersistenceAdapter(jdbcTemplate);
    }

    @Test
    void givenTransactionDataForPaymentOperation_whenPersisted_thenPersistTransactionAndVerifyTransactionId() {
        // Given
        Account savedAccount = accountPersistenceAdapter.save(Account.builder().documentNumber(12345678900L).build());
        OperationType operationType = OperationType.PAYMENT;
        BigDecimal amount = BigDecimal.valueOf(123.456).setScale(2, RoundingMode.HALF_UP);


        Transaction actualTransaction = Transaction.builder()
                .accountId(savedAccount.accountId())
                .amount(amount)
                .operationTypeId(operationType.getId())
                .eventDate(LocalDateTime.now()).build();

        // When
        BalanceBearingTransaction savedTransaction = transactionPersistenceAdapter.save(ExecutableTransaction.from(actualTransaction));

        // Then
        assertThat(savedTransaction).isNotNull();
        assertThat(savedTransaction.transaction()).isNotNull();
        assertThat(savedTransaction.transaction().accountId()).isEqualTo(actualTransaction.accountId());
        assertThat(savedTransaction.transaction().operationTypeId()).isEqualTo(actualTransaction.operationTypeId());
        assertThat(savedTransaction.transaction().amount()).isEqualByComparingTo(amount);
        assertThat(savedTransaction.transaction().eventDate()).isEqualTo(actualTransaction.eventDate());

        TransactionEntity storedTransaction = jdbcTemplate.queryForObject(
                "SELECT * FROM TRANSACTIONS WHERE TRANSACTION_ID = ?",
                TRANSACTION_ROW_MAPPER,
                savedTransaction.transaction().transactionId()
        );

        assertThat(storedTransaction).isNotNull();
        assertThat(storedTransaction.transactionId()).isEqualTo(savedTransaction.transaction().transactionId());
        assertThat(storedTransaction.accountId()).isEqualTo(savedTransaction.transaction().accountId());
        assertThat(storedTransaction.operationTypeId()).isEqualTo(savedTransaction.transaction().operationTypeId());
        assertThat(storedTransaction.balance()).isEqualTo(savedTransaction.balance());
        assertThat(storedTransaction.amount()).isEqualTo(savedTransaction.transaction().amount());
        assertThat(storedTransaction.eventDate()).isEqualTo(savedTransaction.transaction().eventDate());
        assertThat(storedTransaction.balance()).isEqualTo(savedTransaction.balance());
    }

    @ParameterizedTest
    @ValueSource(ints = {1,2,3, 4})
    void givenTransactionForMultipleOpTypes_whenTransactionSaved_thenTransactionIsPersisted(int operationTypeId) {
        //given
        Account savedAccount = accountPersistenceAdapter.save(Account.builder().documentNumber(12345678900L).build());

        BigDecimal amount = ExecutableTransaction.normalizeAmount(OperationType.fromId(operationTypeId), BigDecimal.valueOf(123.456).setScale(2, RoundingMode.HALF_UP));

        Transaction actualTransaction = Transaction.builder()
                .accountId(savedAccount.accountId())
                .amount(amount)
                .operationTypeId(operationTypeId)
                .eventDate(LocalDateTime.now()).build();

        BalanceBearingTransaction savedTransaction =  transactionPersistenceAdapter.save(ExecutableTransaction.from(actualTransaction));

        assertThat(savedTransaction).isNotNull();
        assertThat(savedTransaction.transaction()).isNotNull();
        assertThat(savedTransaction.transaction().accountId()).isEqualTo(actualTransaction.accountId());
        assertThat(savedTransaction.transaction().operationTypeId()).isEqualTo(actualTransaction.operationTypeId());
        assertThat(savedTransaction.transaction().amount()).isEqualByComparingTo(amount);
        assertThat(savedTransaction.transaction().eventDate()).isEqualTo(actualTransaction.eventDate());

        TransactionEntity storedTransaction = jdbcTemplate.queryForObject(
                "SELECT * FROM TRANSACTIONS WHERE TRANSACTION_ID = ?",
                TRANSACTION_ROW_MAPPER,
                savedTransaction.transaction().transactionId());

        assertThat(storedTransaction).isNotNull();
        assertThat(storedTransaction.transactionId()).isEqualTo(savedTransaction.transaction().transactionId());
        assertThat(storedTransaction.accountId()).isEqualTo(savedTransaction.transaction().accountId());
        assertThat(storedTransaction.operationTypeId()).isEqualTo(savedTransaction.transaction().operationTypeId());
        assertThat(storedTransaction.balance()).isEqualTo(savedTransaction.balance());
        assertThat(storedTransaction.amount()).isEqualTo(savedTransaction.transaction().amount());
        assertThat(storedTransaction.eventDate()).isEqualTo(savedTransaction.transaction().eventDate());
        assertThat(storedTransaction.balance()).isEqualTo(savedTransaction.balance());
    }

    @Test
    void givenAccountDoesNotExistInDB_whenTransactionIsPersisted_thenThrowException() {
        // Given
        long accountId = 1L;
        Transaction actualTransaction = Transaction.builder()
                .accountId(accountId)
                .amount(BigDecimal.TEN)
                .operationTypeId(4)
                .eventDate(LocalDateTime.now()).build();

        // When-then
        assertThatThrownBy(() -> transactionPersistenceAdapter.save(ExecutableTransaction.from(actualTransaction)))
                .isInstanceOf(AccountDoesNotExistException.class)
                .hasMessageContaining(Constants.ACCOUNT_NOT_EXISTS_EXCEPTION_MSG.formatted(accountId));
    }

    @Test
    void givenAccountIdAndDebitTransactionsPresentInDb_whenFetchDebitTransactions_thenReturnDebitTransactionsList(){
        Account savedAccount = accountPersistenceAdapter.save(Account.builder().documentNumber(12345678900L).build());

        long accountId = savedAccount.accountId();
        BigDecimal amount = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);


        Transaction purchaseTx1 = Transaction.builder()
                .accountId(accountId)
                .amount(amount)
                .operationTypeId(CASH_PURCHASE.getId())
                .eventDate(LocalDateTime.now()).build();

        Transaction purchaseTx2 = Transaction.builder()
                .accountId(accountId)
                .amount(amount)
                .operationTypeId(INSTALLMENT_PURCHASE.getId())
                .eventDate(LocalDateTime.now()).build();

        Transaction withdrawal = Transaction.builder()
                .accountId(accountId)
                .amount(amount)
                .operationTypeId(WITHDRAWAL.getId())
                .eventDate(LocalDateTime.now()).build();

        Transaction payment = Transaction.builder()
                .accountId(accountId)
                .amount(amount)
                .operationTypeId(PAYMENT.getId())
                .eventDate(LocalDateTime.now()).build();

        BalanceBearingTransaction savedPurchaseTransaction1 = transactionPersistenceAdapter.save(ExecutableTransaction.from(purchaseTx1));
        BalanceBearingTransaction savedPurchaseTransaction2 = transactionPersistenceAdapter.save(ExecutableTransaction.from(purchaseTx2));
        BalanceBearingTransaction savedWithdrawalTransaction = transactionPersistenceAdapter.save(ExecutableTransaction.from(withdrawal));
        BalanceBearingTransaction savedPaymentTransaction = transactionPersistenceAdapter.save(ExecutableTransaction.from(payment));

        List<BalanceBearingTransaction> debitTransactions = transactionPersistenceAdapter.fetchDebitTransactions(accountId);

        assertThat(debitTransactions).hasSize(3);
        assertThat(debitTransactions).doesNotContain(savedPaymentTransaction);
        assertThat(debitTransactions.getFirst().transaction().transactionId()).isEqualTo(savedPurchaseTransaction1.transaction().transactionId());
        assertThat(debitTransactions.getFirst().transaction().accountId()).isEqualTo(savedPurchaseTransaction1.transaction().accountId());
        assertThat(debitTransactions.getFirst().transaction().operationTypeId()).isEqualTo(savedPurchaseTransaction1.transaction().operationTypeId());
        assertThat(debitTransactions.getFirst().transaction().amount()).isEqualTo(savedPurchaseTransaction1.transaction().amount());
        assertThat(debitTransactions.getFirst().balance()).isEqualTo(savedPurchaseTransaction1.balance());

        assertThat(debitTransactions.get(1).transaction().transactionId()).isEqualTo(savedPurchaseTransaction2.transaction().transactionId());
        assertThat(debitTransactions.get(1).transaction().accountId()).isEqualTo(savedPurchaseTransaction2.transaction().accountId());
        assertThat(debitTransactions.get(1).transaction().operationTypeId()).isEqualTo(savedPurchaseTransaction2.transaction().operationTypeId());
        assertThat(debitTransactions.get(1).transaction().amount()).isEqualTo(savedPurchaseTransaction2.transaction().amount());
        assertThat(debitTransactions.get(1).balance()).isEqualTo(savedPurchaseTransaction2.balance());

        assertThat(debitTransactions.get(2).transaction().transactionId()).isEqualTo(savedWithdrawalTransaction.transaction().transactionId());
        assertThat(debitTransactions.get(2).transaction().accountId()).isEqualTo(savedWithdrawalTransaction.transaction().accountId());
        assertThat(debitTransactions.get(2).transaction().operationTypeId()).isEqualTo(savedWithdrawalTransaction.transaction().operationTypeId());
        assertThat(debitTransactions.get(2).transaction().amount()).isEqualTo(savedWithdrawalTransaction.transaction().amount());
        assertThat(debitTransactions.get(2).balance()).isEqualTo(savedWithdrawalTransaction.balance());
    }

    @Test
    void givenAccountIdAndNoDebitTransactionsPresentInDb_whenFetchDebitTransactions_thenReturnEmptyList(){
        Account savedAccount = accountPersistenceAdapter.save(Account.builder().documentNumber(12345678900L).build());

        long accountId = savedAccount.accountId();
        BigDecimal amount = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);

        Transaction payment = Transaction.builder()
                .accountId(accountId)
                .amount(amount)
                .operationTypeId(PAYMENT.getId())
                .eventDate(LocalDateTime.now()).build();

        transactionPersistenceAdapter.save(ExecutableTransaction.from(payment));

        List<BalanceBearingTransaction> debitTransactions = transactionPersistenceAdapter.fetchDebitTransactions(accountId);

        assertThat(debitTransactions).isEmpty();
    }

    @Test
    void givenForExistingTransaction_whenUpdateTransaction_thenUpdateExistingTransaction(){
        Account savedAccount = accountPersistenceAdapter.save(Account.builder().documentNumber(12345678900L).build());

        long accountId = savedAccount.accountId();

        BigDecimal amount = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);

        Transaction payment = Transaction.builder()
                .accountId(accountId)
                .amount(amount)
                .operationTypeId(PAYMENT.getId())
                .eventDate(LocalDateTime.now()).build();

        BalanceBearingTransaction saved = transactionPersistenceAdapter.save(ExecutableTransaction.from(payment));

        ExecutableTransaction transaction2 = ExecutableTransaction.from(saved.transaction(), BigDecimal.ZERO);

        transactionPersistenceAdapter.updateTransaction(transaction2);

        TransactionEntity storedTransaction = jdbcTemplate.queryForObject(
                "SELECT * FROM TRANSACTIONS WHERE TRANSACTION_ID = ?",
                TRANSACTION_ROW_MAPPER,
                saved.transaction().transactionId());

        assertThat(storedTransaction).isNotNull();
        assertThat(storedTransaction.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(storedTransaction.operationTypeId()).isEqualTo(saved.transaction().operationTypeId());
        assertThat(storedTransaction.amount()).isEqualTo(saved.transaction().amount());
    }


    private static LocalDateTime convertToLocalDate(Timestamp eventDate) {
        if(eventDate != null){
            return eventDate.toLocalDateTime();
        }
        return null;
    }
}

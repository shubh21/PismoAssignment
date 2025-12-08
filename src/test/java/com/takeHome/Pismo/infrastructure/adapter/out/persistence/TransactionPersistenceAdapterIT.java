package com.takeHome.Pismo.infrastructure.adapter.out.persistence;

import com.takeHome.Pismo.core.Constants;
import com.takeHome.Pismo.core.domain.model.Account;
import com.takeHome.Pismo.core.domain.model.OperationType;
import com.takeHome.Pismo.core.domain.model.Transaction;
import com.takeHome.Pismo.core.exception.AccountDoesNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.TestPropertySource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import static com.takeHome.Pismo.core.Constants.INVALID_OPERATION_ID_MSG;
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

    private static final  RowMapper<Transaction> TRANSACTION_ROW_MAPPER = (rs, rowNum) ->
            new Transaction(rs.getLong("TRANSACTION_ID"),
                    rs.getLong("ACCOUNT_ID"),
                    rs.getInt("OPERATIONTYPE_ID"),
                    rs.getBigDecimal("AMOUNT"),
                    rs.getTimestamp("EVENT_DATE").toLocalDateTime());


    @BeforeEach
    void setup(){
        transactionPersistenceAdapter = new TransactionPersistenceAdapter(jdbcTemplate);
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

        Transaction savedTransaction = transactionPersistenceAdapter.save(actualTransaction);

        // Then
        assertThat(savedTransaction).isNotNull();
        assertThat(savedTransaction.accountId()).isEqualTo(actualTransaction.accountId());
        assertThat(savedTransaction.operationTypeId()).isEqualTo(actualTransaction.operationTypeId());
        assertThat(savedTransaction.amount()).isEqualByComparingTo(amount);
        assertThat(savedTransaction.eventDate()).isEqualTo(actualTransaction.eventDate());

        Transaction storedTransaction = jdbcTemplate.queryForObject(
                "SELECT * FROM TRANSACTIONS WHERE TRANSACTION_ID = ?",
                TRANSACTION_ROW_MAPPER,
                savedTransaction.transactionId()
        );

        assertThat(storedTransaction).isEqualTo(savedTransaction);

    }

    @Test
    void givenNegativeAmountForPaymentOperation_whenTransactionIsSaved_thenCheckConstraintViolationOccurs() {

        Account savedAccount = accountPersistenceAdapter.save(Account.builder().documentNumber(12345678900L).build());
        BigDecimal amount = BigDecimal.TEN.negate();

        Transaction actualTransaction = Transaction.builder()
                .accountId(savedAccount.accountId())
                .amount(amount)
                .operationTypeId(4)
                .eventDate(LocalDateTime.now()).build();

        assertThatThrownBy(() -> transactionPersistenceAdapter.save(actualTransaction))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("chk_transaction_amount_sign");
    }

    @ParameterizedTest
    @ValueSource(ints = {1,2,3})
    void givenTransactionForPurchaseAndWithdrawalOperations_whenTransactionIsSaved_thenTransactionIsPersisted(int operationTypeId) {
        //given
        Account savedAccount = accountPersistenceAdapter.save(Account.builder().documentNumber(12345678900L).build());

        BigDecimal amount = applySignRule(operationTypeId, BigDecimal.valueOf(123.456).setScale(2, RoundingMode.HALF_UP));

        Transaction actualTransaction = Transaction.builder()
                .accountId(savedAccount.accountId())
                .amount(amount)
                .operationTypeId(operationTypeId)
                .eventDate(LocalDateTime.now()).build();

        Transaction savedTransaction =  transactionPersistenceAdapter.save(actualTransaction);

        assertThat(savedTransaction).isNotNull();
        assertThat(savedTransaction.accountId()).isEqualTo(actualTransaction.accountId());
        assertThat(savedTransaction.operationTypeId()).isEqualTo(actualTransaction.operationTypeId());
        assertThat(savedTransaction.amount()).isEqualByComparingTo(amount);
        assertThat(savedTransaction.eventDate()).isEqualTo(actualTransaction.eventDate());

        Transaction storedTransaction = jdbcTemplate.queryForObject(
                "SELECT * FROM TRANSACTIONS WHERE TRANSACTION_ID = ?",
                TRANSACTION_ROW_MAPPER,
                savedTransaction.transactionId()
        );

        assertThat(storedTransaction).isEqualTo(savedTransaction);
    }

    @ParameterizedTest
    @ValueSource(ints = {1,2,3})
    void givenPositiveAmountForPurchaseAndWithdrawalOperation_whenTransactionIsSaved_thenCheckConstraintViolationOccurs(int operationTypeId) {

        Account savedAccount = accountPersistenceAdapter.save(Account.builder().documentNumber(12345678900L).build());

        Transaction actualTransaction = Transaction.builder()
                .accountId(savedAccount.accountId())
                .amount(BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP))
                .operationTypeId(operationTypeId)
                .eventDate(LocalDateTime.now()).build();

        assertThatThrownBy(() -> transactionPersistenceAdapter.save(actualTransaction))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("chk_transaction_amount_sign");
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
        assertThatThrownBy(() -> transactionPersistenceAdapter.save(actualTransaction))
                .isInstanceOf(AccountDoesNotExistException.class)
                .hasMessageContaining(Constants.ACCOUNT_NOT_EXISTS_EXCEPTION_MSG.formatted(accountId));
    }

    private BigDecimal applySignRule(int operationTypeId, BigDecimal amount) {
        BigDecimal formattedAmount = amount.setScale(2, RoundingMode.HALF_UP);

        return switch (operationTypeId) {
            case 1, 2, 3 -> formattedAmount.compareTo(BigDecimal.ZERO)>0 ? formattedAmount.negate():formattedAmount; //purchase and withdrawal
            case 4 -> formattedAmount.compareTo(BigDecimal.ZERO)<0 ? formattedAmount.negate() : formattedAmount;    // PAYMENT
            default -> throw new IllegalArgumentException(INVALID_OPERATION_ID_MSG.formatted(operationTypeId));
        };
    }
}

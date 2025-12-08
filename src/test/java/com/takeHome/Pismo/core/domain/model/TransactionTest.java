package com.takeHome.Pismo.core.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import static com.takeHome.Pismo.core.Constants.INVALID_ACCOUNT_ID_MSG;
import static com.takeHome.Pismo.core.Constants.INVALID_AMOUNT_VALUE_MSG;
import static com.takeHome.Pismo.core.Constants.INVALID_OPERATION_ID_MSG;
import static com.takeHome.Pismo.core.Constants.INVALID_TRANSACTION_ID_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    private static final long TRANSACTION_ID = 1L;
    private static final long ACCOUNT_ID = 10L;
    private static final OperationType OPERATION_TYPE = OperationType.CASH_PURCHASE;
    private static final BigDecimal AMOUNT = BigDecimal.TEN;


    @Test
    void givenValidValues_whenConstructed_thenFieldsAreSetAndAmountIsScaled() {
        // Given
        LocalDateTime eventDate = LocalDateTime.now();

        // When
        Transaction tx = new Transaction(TRANSACTION_ID, ACCOUNT_ID, OPERATION_TYPE.getId(), AMOUNT, eventDate);

        // Then
        assertThat(tx.transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(tx.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(tx.operationTypeId()).isEqualTo(OPERATION_TYPE.getId());
        assertThat(tx.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(tx.eventDate()).isEqualTo(eventDate);
    }


    @Test
    void givenNullIds_whenConstructed_thenIdsRemainNullAndOtherFieldsAreSet() {
        // Given
        Long transactionId = null;
        LocalDateTime eventDate = LocalDateTime.of(2024, 2, 2, 15, 30);

        // When
        Transaction tx = new Transaction(transactionId, ACCOUNT_ID, OPERATION_TYPE.getId(), AMOUNT, eventDate);

        // Then
        assertThat(tx.transactionId()).isNull();
        assertThat(tx.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(tx.operationTypeId()).isEqualTo(OPERATION_TYPE.getId());
        assertThat(tx.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(tx.eventDate()).isEqualTo(eventDate);
    }

    @Test
    void givenNullEventDate_whenConstructed_thenEventDateSetToNull() {
        // When
        Transaction tx = new Transaction(TRANSACTION_ID, ACCOUNT_ID, OPERATION_TYPE.getId(), AMOUNT, null);

        // Then
        assertThat(tx.transactionId()).isEqualTo(1L);
        assertThat(tx.accountId()).isEqualTo(10L);
        assertThat(tx.operationTypeId()).isEqualTo(OPERATION_TYPE.getId());
        assertThat(tx.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(tx.eventDate()).isNull();
    }


    @ParameterizedTest
    @CsvSource({
             "1, 100, 100.00",
            "2, 100.565, 100.57",
            "3, 100.2345, 100.23",
            "4, 100.2354, 100.24",
    })
    void givenVariousAmountsAndOperationTypes_whenConstructed_thenAmountIsScaledTWithCorrectSign(int OpTypeId, String input, String expected) {
        // Given
        BigDecimal amount = new BigDecimal(input);

        // When
        Transaction tx = new Transaction(
                null,
                ACCOUNT_ID,
                OpTypeId,
                amount,
                LocalDateTime.now()
        );

        assertThat(tx.amount()).isEqualByComparingTo(expected);
    }

    @Test
    void givenNullAmount_whenConstructed_thenIllegalArgumentExceptionThrown() {
        BigDecimal amount = null;
        // When / Then
        assertThatThrownBy(() ->
                new Transaction(null, ACCOUNT_ID, OPERATION_TYPE.getId(), amount, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_AMOUNT_VALUE_MSG.formatted(amount));
    }


    @ParameterizedTest
    @EnumSource(OperationType.class)
    void givenZeroAmount_whenConstructed_thenTransactionModelIsConstructedWithCorrectValues(OperationType operationType) {
        BigDecimal amount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        // When
        Transaction tx = new Transaction(
                null,
                ACCOUNT_ID,
                operationType.getId(),
                amount,
                LocalDateTime.now()
        );

       assertThat(tx.amount()).isEqualTo(amount);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -10L, Long.MIN_VALUE})
    void givenNonPositiveTransactionId_whenConstructed_thenIllegalArgumentExceptionThrown(long invalidId) {
        // When - Then
        assertThatThrownBy(() ->
                new Transaction(invalidId, 1L, OPERATION_TYPE.getId(), AMOUNT, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_TRANSACTION_ID_MSG.formatted(invalidId));
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -10L, Long.MIN_VALUE})
    void givenNonPositiveAccountId_whenConstructed_thenIllegalArgumentExceptionThrown(long invalidAccountId) {
        // When / Then
        assertThatThrownBy(() ->
                new Transaction(TRANSACTION_ID, invalidAccountId, OPERATION_TYPE.getId(), AMOUNT, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_ACCOUNT_ID_MSG.formatted(invalidAccountId));
    }

    @Test
    void givenInvalidOperationTypeId_whenConstructed_thenIllegalArgumentExceptionThrown() {
        // Given
        int invalidOpTypeId = 999;

        // When / Then
        assertThatThrownBy(() ->
                new Transaction(TRANSACTION_ID, ACCOUNT_ID, invalidOpTypeId, AMOUNT, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_OPERATION_ID_MSG.formatted(invalidOpTypeId));
    }


    @Test
    void givenValidValues_whenBuilderUsed_thenTransactionIsCreatedCorrectly() {
        // Given

        LocalDateTime eventDate = LocalDateTime.now();

        // When
        Transaction tx = Transaction.builder()
                .transactionId(TRANSACTION_ID)
                .accountId(ACCOUNT_ID)
                .operationTypeId(OPERATION_TYPE.getId())
                .amount(BigDecimal.valueOf(10.568))
                .eventDate(eventDate)
                .build();

        // Then
        assertThat(tx.transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(tx.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(tx.operationTypeId()).isEqualTo(OPERATION_TYPE.getId());
        assertThat(tx.amount()).isEqualByComparingTo(BigDecimal.valueOf(10.57)); // formatted in constructor
        assertThat(tx.eventDate()).isEqualTo(eventDate);
    }

    @Test
    void givenOnlyRequiredFields_whenBuilderUsed_thenTransactionIsCreatedWithNullOptionalFields() {

        // When
        Transaction tx = Transaction.builder()
                .accountId(ACCOUNT_ID)
                .operationTypeId(OPERATION_TYPE.getId())
                .amount(AMOUNT)
                .build();

        // Then
        assertThat(tx.transactionId()).isNull();
        assertThat(tx.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(tx.operationTypeId()).isEqualTo(OPERATION_TYPE.getId());
        assertThat(tx.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(tx.eventDate()).isNull();
    }

    @Test
    void givenNullIdsSetThroughBuilder_whenBuildCalled_thenIdsRemainNull() {
        // When
        Transaction tx = Transaction.builder()
                .transactionId(null)
                .accountId(ACCOUNT_ID)
                .operationTypeId(OPERATION_TYPE.getId())
                .amount(BigDecimal.ZERO)
                .eventDate(null)
                .build();

        // Then
        assertThat(tx.transactionId()).isNull();
        assertThat(tx.eventDate()).isNull();
    }


    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -10L, Long.MIN_VALUE})
    void givenNonPositiveTransactionId_whenBuilderTransactionIdCalled_thenIllegalArgumentExceptionThrown(long invalidId) {
        // Given
        Transaction.TransactionBuilder builder = Transaction.builder();

        // When / Then
        assertThatThrownBy(() -> builder.transactionId(invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(INVALID_TRANSACTION_ID_MSG.formatted(invalidId));
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -10L, Long.MIN_VALUE})
    void givenNonPositiveAccountId_whenBuilderAccountIdCalled_thenIllegalArgumentExceptionThrown(long invalidAccountId) {
        // Given
        Transaction.TransactionBuilder builder = Transaction.builder();

        // When / Then
        assertThatThrownBy(() -> builder.accountId(invalidAccountId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(INVALID_ACCOUNT_ID_MSG.formatted(invalidAccountId));
    }

    @Test
    void givenInvalidOperationTypeId_whenBuilderOperationTypeIdCalled_thenIllegalArgumentExceptionThrown() {
        // Given
        int invalidOpTypeId = 999;
        Transaction.TransactionBuilder builder = Transaction.builder();

        // When / Then
        assertThatThrownBy(() -> builder.operationTypeId(invalidOpTypeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_OPERATION_ID_MSG.formatted(invalidOpTypeId));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 100, 100.00",
            "2, 100.56, 100.56",
            "3, 100.2345, 100.23",
            "4, 100.235, 100.24",
    })
    void givenVariousAmountsAndOperationTypes_whenBuilderIsCalled_thenAmountIsScaledTWithCorrectSign(int opTypeId, String input, String expected) {
        // Given
        BigDecimal amount = new BigDecimal(input);

        // When
        Transaction tx = Transaction.builder()
                .accountId(ACCOUNT_ID)
                .operationTypeId(opTypeId)
                .amount(amount)
                .build();

        assertThat(tx.operationTypeId()).isEqualTo(opTypeId);
        assertThat(tx.amount()).isEqualByComparingTo(expected);
    }


    @Test
    void givenNullAmount_whenBuilderIsCalled_thenBuildThrowsIllegalArgumentExceptionFromConstructor() {
        // Given
        BigDecimal amount = null;
        Transaction.TransactionBuilder builder = Transaction.builder()
                .accountId(ACCOUNT_ID)
                .operationTypeId(OPERATION_TYPE.getId())
                .amount(null);

        // When / Then
        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_AMOUNT_VALUE_MSG.formatted(amount));
    }

    @Test
    void givenFieldsSetInDifferentOrder_whenBuilderUsed_thenResultingTransactionsAreEqual() {
        // Given
        LocalDateTime eventDate = LocalDateTime.now();

        // When
        Transaction tx1 = Transaction.builder()
                .transactionId(TRANSACTION_ID)
                .accountId(ACCOUNT_ID)
                .operationTypeId(OPERATION_TYPE.getId())
                .amount(AMOUNT)
                .eventDate(eventDate)
                .build();

        Transaction tx2 = Transaction.builder()
                .amount(AMOUNT)
                .eventDate(eventDate)
                .operationTypeId(OPERATION_TYPE.getId())
                .accountId(ACCOUNT_ID)
                .transactionId(TRANSACTION_ID)
                .build();

        // Then
        assertThat(tx1).isEqualTo(tx2);
        assertThat(tx1.transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(tx1.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(tx1.operationTypeId()).isEqualTo(OPERATION_TYPE.getId());
        assertThat(tx1.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(tx1.eventDate()).isEqualTo(eventDate);
    }

    @Test
    void givenTwoTransactionsWithSameValues_whenCompared_thenTheyAreEqual() {
        // Given
        LocalDateTime eventDate = LocalDateTime.now();

        Transaction tx1 = new Transaction(1L, 10L, OperationType.CASH_PURCHASE.getId(), new BigDecimal("10.00"), eventDate);
        Transaction tx2 = Transaction.builder().transactionId(1L).accountId(10L)
                .operationTypeId(OperationType.CASH_PURCHASE.getId())
                .amount(BigDecimal.TEN).eventDate(eventDate).build();

        // Then
        assertThat(tx1).isEqualTo(tx2);
        assertThat(tx1.hashCode()).isEqualTo(tx2.hashCode());
    }
}
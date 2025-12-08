package com.takeHome.Pismo.core.contract.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionResultTest {

    private static final long TRANSACTION_ID = 1L;
    private static final long ACCOUNT_ID = 100L;
    private static final int OPERATIONTYPE_ID = 4;
    private static final BigDecimal AMOUNT = new BigDecimal("123.45");

    @Test
    void givenValidValues_whenBuilderUsed_thenTransactionResultIsCreatedCorrectly() {
        LocalDateTime eventDate = LocalDateTime.now();
        // When
        TransactionResult result = TransactionResult.builder()
                .transactionId(TRANSACTION_ID)
                .accountId(ACCOUNT_ID)
                .operationTypeId(OPERATIONTYPE_ID)
                .amount(AMOUNT)
                .eventDate(eventDate)
                .build();

        // Then
        assertThat(result.transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(result.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.operationTypeId()).isEqualTo(OPERATIONTYPE_ID);
        assertThat(result.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(result.eventDate()).isEqualTo(eventDate);
    }

    @Test
    void givenValidValues_whenConstructorCalled_thenTransactionResultIsCreatedCorrectly() {
        // Given

        LocalDateTime eventDate = LocalDateTime.now();

        // When
        TransactionResult result = new TransactionResult(
                TRANSACTION_ID,
                ACCOUNT_ID,
                OPERATIONTYPE_ID,
                AMOUNT,
                eventDate
        );

        // Then
        assertThat(result.transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(result.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.operationTypeId()).isEqualTo(OPERATIONTYPE_ID);
        assertThat(result.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(result.eventDate()).isEqualTo(eventDate);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 100, 1, 10.00",
            "2, 200, 4, -50.75",
            "3, 300, 7, 0.00"
    })
    void givenMultipleValidCombinations_whenBuilderUsed_thenCorrectValuesAreStored(
            long transactionId,
            long accountId,
            int operationTypeId,
            String amountStr
    ) {
        // Given
        BigDecimal amount = new BigDecimal(amountStr);
        LocalDateTime eventDate = LocalDateTime.now();

        // When
        TransactionResult result = TransactionResult.builder()
                .transactionId(transactionId)
                .accountId(accountId)
                .operationTypeId(operationTypeId)
                .amount(amount)
                .eventDate(eventDate)
                .build();

        // Then
        assertThat(result.transactionId()).isEqualTo(transactionId);
        assertThat(result.accountId()).isEqualTo(accountId);
        assertThat(result.operationTypeId()).isEqualTo(operationTypeId);
        assertThat(result.amount()).isEqualByComparingTo(amount);
        assertThat(result.eventDate()).isEqualTo(eventDate);
    }

    @Test
    void givenLargeValues_whenBuilderUsed_thenValuesAreStoredCorrectly() {
        // Given
        long transactionId = Long.MAX_VALUE;
        long accountId = Long.MAX_VALUE;
        int operationTypeId = Integer.MAX_VALUE;
        BigDecimal amount = new BigDecimal("9999999999999.99");
        LocalDateTime eventDate = LocalDateTime.MAX.minusYears(1); // avoid overflow

        // When
        TransactionResult result = TransactionResult.builder()
                .transactionId(transactionId)
                .accountId(accountId)
                .operationTypeId(operationTypeId)
                .amount(amount)
                .eventDate(eventDate)
                .build();

        // Then
        assertThat(result.transactionId()).isEqualTo(transactionId);
        assertThat(result.accountId()).isEqualTo(accountId);
        assertThat(result.operationTypeId()).isEqualTo(operationTypeId);
        assertThat(result.amount()).isEqualByComparingTo(amount);
        assertThat(result.eventDate()).isEqualTo(eventDate);
    }

    @Test
    void givenOnlyIdsSet_whenBuilderUsed_thenOtherFieldsDefaultToNullOrZero() {
        // When
        TransactionResult result = TransactionResult.builder()
                .transactionId(TRANSACTION_ID)
                .accountId(ACCOUNT_ID)
                .build();

        // Then
        assertThat(result.transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(result.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.operationTypeId()).isZero();
        assertThat(result.amount()).isNull();
        assertThat(result.eventDate()).isNull();
    }

    @Test
    void givenFieldsSetInDifferentOrder_whenBuilderUsed_thenResultIsSame() {
        // Given
        LocalDateTime eventDate = LocalDateTime.of(2024, 1, 1, 10, 0);

        // When
        TransactionResult resultOrder1 = TransactionResult.builder()
                .transactionId(TRANSACTION_ID)
                .accountId(ACCOUNT_ID)
                .operationTypeId(OPERATIONTYPE_ID)
                .amount(AMOUNT)
                .eventDate(eventDate)
                .build();

        TransactionResult resultOrder2 = TransactionResult.builder()
                .eventDate(eventDate)
                .amount(AMOUNT)
                .operationTypeId(OPERATIONTYPE_ID)
                .accountId(ACCOUNT_ID)
                .transactionId(TRANSACTION_ID)
                .build();

        // Then
        assertThat(resultOrder1).isEqualTo(resultOrder2);
        assertThat(resultOrder1.transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(resultOrder1.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(resultOrder1.operationTypeId()).isEqualTo(OPERATIONTYPE_ID);
        assertThat(resultOrder1.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(resultOrder1.eventDate()).isEqualTo(eventDate);
    }
}

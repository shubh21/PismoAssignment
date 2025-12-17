package com.takeHome.Pismo.core.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

public class BalanceBearingTransactionTest {

    private static final long ACCOUNT_ID = 10L;
    private static final OperationType OPERATION_TYPE = OperationType.CASH_PURCHASE;
    private static final BigDecimal AMOUNT = BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP);

    @ParameterizedTest
    @ValueSource(ints = {1,2,3})
    void givenWithdrawalTransactions_whenCreated_thenAmountAndBalanceTurnNegative(int opTypeId) {
        Transaction transaction = Transaction.builder()
                .accountId(ACCOUNT_ID)
                .amount(AMOUNT)
                .operationTypeId(opTypeId)
                .build();

        ExecutableTransaction executableTransaction = ExecutableTransaction.from(transaction);

        assertThat(executableTransaction.transaction().amount()).isEqualTo(AMOUNT.negate());
        assertThat(executableTransaction.balance()).isEqualTo(AMOUNT.negate());

        assertThat(executableTransaction.transaction().accountId()).isEqualTo(transaction.accountId());
        assertThat(executableTransaction.transaction().operationTypeId()).isEqualTo(transaction.operationTypeId());
        assertThat(executableTransaction.transaction().amount()).isEqualTo(transaction.amount().negate());
    }


    @Test
    void givenPayment_whenCreated_thenAmountAndBalanceStaysPositive() {
        Transaction transaction = Transaction.builder()
                .accountId(ACCOUNT_ID)
                .amount(AMOUNT)
                .operationTypeId(OperationType.PAYMENT.getId())
                .build();

        ExecutableTransaction executableTransaction = ExecutableTransaction.from(transaction);

        assertThat(executableTransaction.balance()).isEqualTo(AMOUNT);
        assertThat(executableTransaction.transaction().amount()).isEqualTo(AMOUNT);
        assertThat(executableTransaction.transaction()).isEqualTo(transaction);

    }

    @Test
    void givenNonZeroBalance_whenCheckSettled_thenFalse() {
        Transaction transaction = Transaction.builder()
                .accountId(ACCOUNT_ID)
                .amount(BigDecimal.ONE)
                .operationTypeId(OPERATION_TYPE.getId())
                .build();

        ExecutableTransaction executableTransaction = ExecutableTransaction.from(transaction);

        assertThat(executableTransaction.transaction().accountId()).isEqualTo(transaction.accountId());
        assertThat(executableTransaction.transaction().operationTypeId()).isEqualTo(transaction.operationTypeId());
        assertThat(executableTransaction.transaction().amount()).isEqualTo(transaction.amount().negate());
        assertThat(executableTransaction.balance()).isEqualByComparingTo(BigDecimal.ONE.negate().setScale(2, RoundingMode.HALF_UP));
        assertThat(executableTransaction.isSettled()).isFalse();
    }

    @Test
    void givenZeroBalance_whenCheckSettled_thenTrue() {
        Transaction transaction = Transaction.builder()
                .accountId(ACCOUNT_ID)
                .amount(BigDecimal.ZERO)
                .operationTypeId(OPERATION_TYPE.getId())
                .build();

        ExecutableTransaction executableTransaction = ExecutableTransaction.from(transaction);

        assertThat(executableTransaction.transaction()).isEqualTo(transaction);
        assertThat(executableTransaction.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(executableTransaction.isSettled()).isTrue();
    }

    @Test
    void givenPaymentOperationType_whenIsPaymentChecked_thenItReturnsTrue(){
        Transaction transaction = Transaction.builder()
                .accountId(ACCOUNT_ID)
                .amount(BigDecimal.ZERO)
                .operationTypeId(OperationType.PAYMENT.getId())
                .build();

        ExecutableTransaction executableTransaction = ExecutableTransaction.from(transaction);

        assertThat(executableTransaction.isPayment()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {1,2,3})
    void givenNonPaymentOperationType_whenIsPaymentChecked_thenItReturnsFalse(int opTypeId){
        Transaction transaction = Transaction.builder()
                .accountId(ACCOUNT_ID)
                .amount(BigDecimal.ZERO)
                .operationTypeId(opTypeId)
                .build();

        ExecutableTransaction executableTransaction = ExecutableTransaction.from(transaction);

        assertThat(executableTransaction.isPayment()).isFalse();
    }
}

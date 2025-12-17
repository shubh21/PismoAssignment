package com.takeHome.Pismo.core.domain.usecase;

import com.takeHome.Pismo.core.domain.model.ExecutableTransaction;
import com.takeHome.Pismo.core.domain.model.OperationType;
import com.takeHome.Pismo.core.domain.model.Transaction;
import com.takeHome.Pismo.core.usecase.BalanceDischargeUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;


public class BalanceDischargeUseCaseTest {

    private BalanceDischargeUseCase balanceDischargeUseCase;

    private static final long ACCOUNT_ID = 10L;
    private static final OperationType OPERATION_TYPE = OperationType.CASH_PURCHASE;
    private static final BigDecimal AMOUNT = BigDecimal.TEN;

    @BeforeEach
    void setup(){
      balanceDischargeUseCase = new BalanceDischargeUseCase();
    }

    @Test
    void givenPaymentBalanceCoversDebitBalance_whenDischarged_thenDebitIsSettled() {
        Transaction debitTransaction = Transaction.builder()
                .transactionId(1L)
                .accountId(ACCOUNT_ID)
                .amount(AMOUNT)
                .operationTypeId(OPERATION_TYPE.getId())
                .build();

        ExecutableTransaction debit = ExecutableTransaction.from(debitTransaction);

        ExecutableTransaction payment = ExecutableTransaction.from(Transaction.builder()
                        .transactionId(2L)
                .accountId(ACCOUNT_ID)
                .amount(AMOUNT)
                .operationTypeId(OperationType.PAYMENT.getId())
                .build());

        balanceDischargeUseCase.discharge(payment, Collections.singletonList(debit));

        assertThat(debit.balance()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        assertThat(payment.balance()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void givenPaymentBalanceExceedsDebitBalance_whenDischarged_thenDebitIsSettled() {
        BigDecimal paymentValue = BigDecimal.valueOf(15);
        Transaction debitTransaction = Transaction.builder()
                .transactionId(1L)
                .accountId(ACCOUNT_ID)
                .amount(AMOUNT)
                .operationTypeId(OPERATION_TYPE.getId())
                .eventDate(LocalDateTime.now())
                .build();

        ExecutableTransaction debit = ExecutableTransaction.from(debitTransaction);

        ExecutableTransaction payment = ExecutableTransaction.from(Transaction.builder()
                .transactionId(2L)
                .accountId(ACCOUNT_ID)
                .amount(paymentValue)
                .operationTypeId(OperationType.PAYMENT.getId())
                .eventDate(LocalDateTime.now())
                .build());

        balanceDischargeUseCase.discharge(payment, Collections.singletonList(debit));

        assertThat(debit.balance()).isZero();
        assertThat(payment.balance()).isNotZero();
        assertThat(payment.balance()).isEqualByComparingTo(paymentValue.subtract(debitTransaction.amount()).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void givenPartialPayment_whenDischarged_thenDebitReduced() {
        ExecutableTransaction debit = ExecutableTransaction.from(Transaction.builder()
                        .accountId(ACCOUNT_ID)
                        .amount(AMOUNT)
                        .operationTypeId(OPERATION_TYPE.getId())
                        .eventDate(LocalDateTime.now())
                        .build());

        ExecutableTransaction payment = ExecutableTransaction
                .from(Transaction.builder()
                .accountId(20L)
                .amount(BigDecimal.valueOf(5))
                .operationTypeId(OperationType.PAYMENT.getId())
                        .eventDate(LocalDateTime.now())
                .build());

        balanceDischargeUseCase.discharge(payment, Collections.singletonList(debit));

        assertThat(debit.balance()).isNotZero();
        assertThat(debit.balance()).isEqualByComparingTo(BigDecimal.valueOf(-5).setScale(2, RoundingMode.HALF_UP));
        assertThat(payment.balance()).isZero();
    }

    @Test
    void givenMultipleDebitsExceedingPaymentBalance_whenDischarged_thenDebitIsReducedAndPaymentBalanceIsUpdated() {
        ExecutableTransaction debit1 = ExecutableTransaction.from(Transaction.builder()
                .accountId(ACCOUNT_ID)
                .amount(AMOUNT)
                .operationTypeId(OPERATION_TYPE.getId())
                .eventDate(LocalDateTime.now())
                .build());

        ExecutableTransaction debit2 = ExecutableTransaction.from(Transaction.builder()
                .accountId(ACCOUNT_ID)
                .amount(AMOUNT)
                .operationTypeId(OPERATION_TYPE.getId())
                .eventDate(LocalDateTime.now())
                .build());

        ExecutableTransaction payment = ExecutableTransaction
                .from(Transaction.builder()
                        .accountId(ACCOUNT_ID)
                        .amount(BigDecimal.valueOf(15))
                        .operationTypeId(OperationType.PAYMENT.getId())
                        .build());

        balanceDischargeUseCase.discharge(payment, new ArrayList<>(List.of(debit1, debit2)));

        assertThat(debit1.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(debit2.balance()).isNotEqualByComparingTo(BigDecimal.ZERO);
        assertThat(debit2.balance()).isEqualByComparingTo(BigDecimal.valueOf(-5).setScale(2, RoundingMode.HALF_UP));
        assertThat(payment.balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void givenMultipleDebitsBalanceLessThanPaymentBalance_whenDischarged_thenDebitIsReducedAndPaymentBalanceIsUpdated() {
        ExecutableTransaction debit1 = ExecutableTransaction.from(Transaction.builder()
                .accountId(ACCOUNT_ID)
                .amount(AMOUNT)
                .operationTypeId(OPERATION_TYPE.getId())
                .eventDate(LocalDateTime.now())
                .build());
        ExecutableTransaction debit2 = ExecutableTransaction.from(Transaction.builder()
                .accountId(ACCOUNT_ID)
                .amount(AMOUNT)
                .operationTypeId(OPERATION_TYPE.getId())
                .eventDate(LocalDateTime.now())
                .build());

        ExecutableTransaction payment = ExecutableTransaction
                .from(Transaction.builder()
                        .accountId(ACCOUNT_ID)
                        .amount(BigDecimal.valueOf(30))
                        .operationTypeId(OperationType.PAYMENT.getId())
                        .build());

        balanceDischargeUseCase.discharge(payment, new ArrayList<>(List.of(debit1, debit2)));

        assertThat(debit1.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(debit2.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(payment.balance()).isNotEqualByComparingTo(BigDecimal.ZERO);

        assertThat(payment.balance()).isEqualByComparingTo(BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP));
    }
}

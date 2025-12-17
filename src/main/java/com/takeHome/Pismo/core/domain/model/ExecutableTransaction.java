package com.takeHome.Pismo.core.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExecutableTransaction implements BalanceBearingTransaction{

    private final Transaction transaction;
    private BigDecimal balance;

    public ExecutableTransaction(Transaction transaction, BigDecimal balance) {
        this.transaction = transaction;
        this.balance = balance;
    }

    public static ExecutableTransaction from(Transaction transaction) {
        BigDecimal signedAmount = normalizeAmount(
                OperationType.fromId(transaction.operationTypeId()),
                transaction.amount());

        Transaction normalizedTxn = Transaction.builder()
                .transactionId(transaction.transactionId())
                .accountId(transaction.accountId())
                .operationTypeId(transaction.operationTypeId())
                .amount(signedAmount)
                .eventDate(transaction.eventDate())
                .build();

        return new ExecutableTransaction(normalizedTxn, signedAmount);
    }

    public static ExecutableTransaction from(Transaction transaction, BigDecimal balance) {

        return new ExecutableTransaction(transaction, balance);
    }


    @Override
    public Transaction transaction() {
        return transaction;
    }

    @Override
    public BigDecimal balance() {
        return balance;
    }

    @Override
    public boolean isSettled() {
        return balance.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public void applyPayment(BigDecimal balance) {
        this.balance = this.balance.add(balance).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean isPayment() {
        return transaction.operationTypeId() == OperationType.PAYMENT.getId();
    }

    public static BigDecimal normalizeAmount(OperationType type, BigDecimal amount) {
        return switch (type) {
            case CASH_PURCHASE, INSTALLMENT_PURCHASE, WITHDRAWAL ->
                    amount.compareTo(BigDecimal.ZERO) > 0 ? amount.negate() : amount;
            case PAYMENT ->
                    amount.compareTo(BigDecimal.ZERO) < 0 ? amount.negate() : amount;
        };
    }
}

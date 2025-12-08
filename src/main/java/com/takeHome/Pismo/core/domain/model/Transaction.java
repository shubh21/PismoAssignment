package com.takeHome.Pismo.core.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static com.takeHome.Pismo.core.Constants.INVALID_ACCOUNT_ID_MSG;
import static com.takeHome.Pismo.core.Constants.INVALID_AMOUNT_VALUE_MSG;
import static com.takeHome.Pismo.core.Constants.INVALID_TRANSACTION_ID_MSG;

public record Transaction(
        Long transactionId,
        Long accountId,
        int operationTypeId,
        BigDecimal amount,
        LocalDateTime eventDate
) {
    public Transaction(Long transactionId, Long accountId, int operationTypeId, BigDecimal amount, LocalDateTime eventDate) {
        validateTransactionId(transactionId);
        validateAccountId(accountId);
        validateOperationTypeId(operationTypeId);
        validateAmount(amount);
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.operationTypeId = operationTypeId;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.eventDate = eventDate;
    }

    private static void validateTransactionId(Long transactionId) {
        if (transactionId != null && transactionId <= 0L) {
            throw new IllegalArgumentException(INVALID_TRANSACTION_ID_MSG.formatted(transactionId));
        }
    }

    private static void validateAccountId(Long accountId) {
        if (accountId == null || accountId <= 0L) {
            throw new IllegalArgumentException(INVALID_ACCOUNT_ID_MSG.formatted(accountId));
        }
    }

    private static void validateOperationTypeId(int operationTypeId) {
        OperationType.fromId(operationTypeId);
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null ) {
            throw new IllegalArgumentException(INVALID_AMOUNT_VALUE_MSG.formatted(amount));
        }
    }

    public static class TransactionBuilder {
        private Long transactionId;
        private Long accountId;
        private int operationTypeId;
        private BigDecimal amount;
        private LocalDateTime eventDate;

        public TransactionBuilder transactionId(Long transactionId) {
            validateTransactionId(transactionId);
            this.transactionId = transactionId;
            return this;
        }

        public TransactionBuilder accountId(Long accountId) {
            validateAccountId(accountId);
            this.accountId = accountId;
            return this;
        }

        public TransactionBuilder operationTypeId(int operationTypeId) {
            validateOperationTypeId(operationTypeId);
            this.operationTypeId = operationTypeId;
            return this;
        }

        public TransactionBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public TransactionBuilder eventDate(LocalDateTime eventDate) {
            this.eventDate = eventDate;
            return this;
        }

        public Transaction build() {
            return new Transaction(transactionId, accountId, operationTypeId, amount, eventDate);
        }
    }

    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }
}

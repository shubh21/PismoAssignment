package com.takeHome.Pismo.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionEntity {

    private Long transactionId;
    private final Long accountId;
    private final int operationTypeId;
    private final BigDecimal amount;
    private final BigDecimal balance;
    private final LocalDateTime eventDate;

    public TransactionEntity(Long transactionId, Long accountId, int operationTypeId, BigDecimal amount, BigDecimal balance, LocalDateTime eventDate) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.operationTypeId = operationTypeId;
        this.amount = amount;
        this.balance = balance;
        this.eventDate = eventDate;
    }

    public static class TransactionEntityBuilder {

        private Long transactionId;
        private Long accountId;
        private int operationTypeId;
        private BigDecimal amount;
        private BigDecimal balance;
        private LocalDateTime eventDate;

        public TransactionEntityBuilder transactionId(Long transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public TransactionEntityBuilder accountId(Long accountId) {
            this.accountId = accountId;
            return this;
        }

        public TransactionEntityBuilder operationTypeId(int operationTypeId) {
            this.operationTypeId = operationTypeId;
            return this;
        }

        public TransactionEntityBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public TransactionEntityBuilder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public TransactionEntityBuilder eventDate(LocalDateTime eventDate) {
            this.eventDate = eventDate;
            return this;
        }

        public TransactionEntity build() {
            return new TransactionEntity(transactionId, accountId, operationTypeId, amount, balance, eventDate);
        }
    }

    public static TransactionEntityBuilder builder() {
        return new TransactionEntityBuilder();
    }


    public Long transactionId() {
        return transactionId;
    }

    public Long accountId() {
        return accountId;
    }

    public int operationTypeId() {
        return operationTypeId;
    }

    public BigDecimal amount() {
        return amount;
    }

    public BigDecimal balance() {
        return balance;
    }

    public LocalDateTime eventDate() {
        return eventDate;
    }

    public void updateTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
}

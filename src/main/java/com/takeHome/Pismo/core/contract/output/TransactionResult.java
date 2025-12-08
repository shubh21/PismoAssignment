package com.takeHome.Pismo.core.contract.output;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResult( long transactionId, long accountId, int operationTypeId, BigDecimal amount, LocalDateTime eventDate) {

    public static class TransactionResultBuilder {
        private long transactionId;
        private long accountId;
        private int operationTypeId;
        private BigDecimal amount;
        private LocalDateTime eventDate;

        public TransactionResultBuilder transactionId(long transactionId){
            this.transactionId = transactionId;
            return this;
        }

        public TransactionResultBuilder accountId(long accountId){
            this.accountId = accountId;
            return this;
        }

        public TransactionResultBuilder operationTypeId(int operationTypeId){
            this.operationTypeId = operationTypeId;
            return this;
        }

        public TransactionResultBuilder amount(BigDecimal amount){
            this.amount = amount;
            return this;
        }

        public TransactionResultBuilder eventDate(LocalDateTime eventDate){
            this.eventDate = eventDate;
            return this;
        }

        public TransactionResult build(){
            return new TransactionResult(transactionId, accountId, operationTypeId, amount, eventDate);
        }
    }

    public static TransactionResultBuilder builder(){
        return new TransactionResultBuilder();
    }
}

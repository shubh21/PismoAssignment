package com.takeHome.Pismo.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse (
        @JsonProperty("transaction_id") long transactionId,
        @JsonProperty("account_id") long accountId,
        @JsonProperty("operationType_Id") int operationTypeId,
        BigDecimal amount,
        @JsonProperty("event_date") LocalDateTime eventDate){

    public static class TransactionResponseBuilder {
        private long transactionId;
        private long accountId;
        private int operationTypeId;
        private BigDecimal amount;
        private LocalDateTime eventDate;

        public TransactionResponseBuilder transactionId(long transactionId){
            this.transactionId = transactionId;
            return this;
        }

        public TransactionResponseBuilder accountId(long accountId){
            this.accountId = accountId;
            return this;
        }

        public TransactionResponseBuilder operationTypeId(int operationTypeId){
            this.operationTypeId = operationTypeId;
            return this;
        }

        public TransactionResponseBuilder amount(BigDecimal amount){
            this.amount = amount;
            return this;
        }

        public TransactionResponseBuilder eventDate(LocalDateTime eventDate){
            this.eventDate = eventDate;
            return this;
        }

        public TransactionResponse build(){
            return new TransactionResponse(transactionId, accountId, operationTypeId, amount, eventDate);
        }
    }

    public static TransactionResponseBuilder builder(){
        return new TransactionResponseBuilder();
    }
}

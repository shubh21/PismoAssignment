package com.takeHome.Pismo.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccountResponse( @JsonProperty("account_id") long accountId,
                               @JsonProperty("document_number") long documentNumber) {

    public static class AccountResponseBuilder{
        private long accountId;
        private long documentNumber;

        public AccountResponseBuilder documentNumber(long documentNumber){
            this.documentNumber = documentNumber;
            return this;
        }

        public AccountResponseBuilder accountId(long accountId){
            this.accountId = accountId;
            return this;
        }

        public AccountResponse build(){
            return new AccountResponse(accountId, documentNumber);
        }
    }

    public static AccountResponseBuilder builder(){
        return new AccountResponseBuilder();
    }
}

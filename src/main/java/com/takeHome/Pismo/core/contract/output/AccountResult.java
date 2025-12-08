package com.takeHome.Pismo.core.contract.output;

public record AccountResult(long accountId, long documentNumber) {

    public static class AccountResultBuilder{
        private long accountId;
        private long documentNumber;

        public AccountResultBuilder accountId(long accountId){
            this.accountId = accountId;
            return this;
        }

        public AccountResultBuilder documentNumber(long documentNumber){
            this.documentNumber = documentNumber;
            return this;
        }

        public AccountResult build(){
            return new AccountResult(accountId, documentNumber);
        }
    }

    public static AccountResultBuilder builder(){
        return new AccountResultBuilder();
    }
}

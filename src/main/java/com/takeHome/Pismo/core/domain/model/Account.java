package com.takeHome.Pismo.core.domain.model;

import static com.takeHome.Pismo.core.Constants.INVALID_ACCOUNT_ID_MSG;
import static com.takeHome.Pismo.core.Constants.INVALID_DOCUMENT_NUMBER_MSG;

public record Account(Long accountId, Long documentNumber) {

    public Account {
        validateAccountId(accountId);
        validateDocumentNumber(documentNumber);
    }

    private static void validateAccountId(Long accountId) {
        if (accountId != null && accountId <= 0L) {
            throw new IllegalArgumentException(String.format(INVALID_ACCOUNT_ID_MSG, accountId));
        }
    }

    private static void validateDocumentNumber(Long documentNumber) {
        if (documentNumber != null && documentNumber <= 0L) {
            throw new IllegalArgumentException(String.format(INVALID_DOCUMENT_NUMBER_MSG, documentNumber));
        }
    }

    public static class AccountBuilder {
        private Long accountId;
        private Long documentNumber;

        public AccountBuilder accountId(Long accountId){
            validateAccountId(accountId);
            this.accountId = accountId;
            return this;
        }

        public AccountBuilder documentNumber(Long documentNumber){
            validateDocumentNumber(documentNumber);
            this.documentNumber = documentNumber;
            return this;
        }

        public Account build(){
            return new Account(accountId, documentNumber);
        }
    }

    public static AccountBuilder builder(){
        return new AccountBuilder();
    }
}

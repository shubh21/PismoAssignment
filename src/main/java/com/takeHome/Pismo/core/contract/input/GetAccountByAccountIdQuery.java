package com.takeHome.Pismo.core.contract.input;

import static com.takeHome.Pismo.core.Constants.INVALID_ACCOUNT_ID_MSG;

public record GetAccountByAccountIdQuery(long accountId) {

    public GetAccountByAccountIdQuery {

        if (accountId <= 0) {
            throw new IllegalArgumentException(String.format(INVALID_ACCOUNT_ID_MSG, accountId));
        }

    }
}

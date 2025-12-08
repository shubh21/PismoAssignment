package com.takeHome.Pismo.core.usecase;

import com.takeHome.Pismo.core.contract.input.CreateAccountCommand;
import com.takeHome.Pismo.core.contract.input.GetAccountByAccountIdQuery;
import com.takeHome.Pismo.core.contract.output.AccountResult;
import com.takeHome.Pismo.core.domain.port.in.AccountManagementPort;
import com.takeHome.Pismo.core.domain.port.out.AccountPersistencePort;
import com.takeHome.Pismo.core.exception.AccountNotFoundException;
import com.takeHome.Pismo.core.domain.model.Account;

import static com.takeHome.Pismo.core.Constants.ACCOUNT_NOT_FOUND_EXCEPTION_MSG;

public class AccountManagementUseCase implements AccountManagementPort {

    private final AccountPersistencePort accountPersistencePort;

    public AccountManagementUseCase(AccountPersistencePort accountPersistencePort) {
        this.accountPersistencePort = accountPersistencePort;
    }

    @Override
    public AccountResult saveAccount(CreateAccountCommand createAccountCommand){

        Account account = Account.builder()
                            .documentNumber(createAccountCommand.documentNumber())
                            .build();

        Account savedAccount = accountPersistencePort.save(account);

        return mapAccountToResult(savedAccount);
    }

    @Override
    public AccountResult retrieveAccount(GetAccountByAccountIdQuery accountQuery){

        Account account = accountPersistencePort.retrieve(accountQuery.accountId())
                        .orElseThrow(() ->
                            new AccountNotFoundException(ACCOUNT_NOT_FOUND_EXCEPTION_MSG.formatted(accountQuery.accountId())));

        return mapAccountToResult(account);
    }

    private AccountResult mapAccountToResult(Account account) {
        return AccountResult.builder()
                .accountId(account.accountId())
                .documentNumber(account.documentNumber())
                .build();
    }
}

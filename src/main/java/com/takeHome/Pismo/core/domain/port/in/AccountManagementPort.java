package com.takeHome.Pismo.core.domain.port.in;

import com.takeHome.Pismo.core.contract.input.CreateAccountCommand;
import com.takeHome.Pismo.core.contract.input.GetAccountByAccountIdQuery;
import com.takeHome.Pismo.core.contract.output.AccountResult;

public interface AccountManagementPort {
    AccountResult saveAccount(CreateAccountCommand createAccountCommand);
    AccountResult retrieveAccount(GetAccountByAccountIdQuery accountQuery);
}

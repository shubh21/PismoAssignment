package com.takeHome.Pismo.core.domain.port.out;

import com.takeHome.Pismo.core.domain.model.Account;

import java.util.Optional;

public interface AccountPersistencePort {
    Account save(Account account);

    Optional<Account> retrieve(long accountId);
}

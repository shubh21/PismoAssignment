package com.takeHome.Pismo.infrastructure.adapter.in.web;

import com.takeHome.Pismo.core.contract.input.CreateAccountCommand;
import com.takeHome.Pismo.core.contract.input.GetAccountByAccountIdQuery;
import com.takeHome.Pismo.core.contract.output.AccountResult;
import com.takeHome.Pismo.core.domain.port.in.AccountManagementPort;
import com.takeHome.Pismo.infrastructure.adapter.in.web.dto.AccountResponse;
import com.takeHome.Pismo.infrastructure.adapter.in.web.dto.CreateAccountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountsController {

    private final AccountManagementPort accountManagementPort;

    public AccountsController(AccountManagementPort accountManagementPort) {
        this.accountManagementPort = accountManagementPort;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@RequestBody CreateAccountRequest request){
        CreateAccountCommand createAccountCommand = new CreateAccountCommand(request.documentNumber());

        AccountResult accountResult = accountManagementPort.saveAccount(createAccountCommand);

        AccountResponse accountResponse = WebResponseMapper.toAccountResponse(accountResult);

        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }

    @GetMapping("/{account_id}")
    public ResponseEntity<AccountResponse> retrieve(@PathVariable("account_id") long accountId){

        GetAccountByAccountIdQuery accountQuery = new GetAccountByAccountIdQuery(accountId);

        AccountResult accountResult = accountManagementPort.retrieveAccount(accountQuery);

        AccountResponse accountResponse = WebResponseMapper.toAccountResponse(accountResult);

        return ResponseEntity.status(HttpStatus.OK).body(accountResponse);
    }
}

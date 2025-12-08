package com.takeHome.Pismo.infrastructure.adapter.in.web;

import com.takeHome.Pismo.core.contract.input.CreateTransactionCommand;
import com.takeHome.Pismo.core.contract.output.TransactionResult;
import com.takeHome.Pismo.core.domain.port.in.TransactionManagementPort;
import com.takeHome.Pismo.infrastructure.adapter.in.web.dto.CreateTransactionRequest;
import com.takeHome.Pismo.infrastructure.adapter.in.web.dto.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionsController {

    private final TransactionManagementPort transactionManagementPort;

    public TransactionsController(TransactionManagementPort transactionManagementPort) {
        this.transactionManagementPort = transactionManagementPort;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> transaction(@Valid @RequestBody CreateTransactionRequest request) {

        CreateTransactionCommand createTransactionCommand = CreateTransactionCommand.fromRequest(request);

        TransactionResult transactionResult = transactionManagementPort.saveTransaction(createTransactionCommand);

        TransactionResponse transactionResponse = WebResponseMapper.toTransactionResponse(transactionResult);

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionResponse);
    }
}

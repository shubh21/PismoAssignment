package com.takeHome.Pismo.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeHome.Pismo.core.contract.input.CreateTransactionCommand;
import com.takeHome.Pismo.core.contract.output.TransactionResult;
import com.takeHome.Pismo.core.domain.port.in.TransactionManagementPort;
import com.takeHome.Pismo.infrastructure.adapter.in.web.advice.GlobalExceptionHandler;
import com.takeHome.Pismo.infrastructure.adapter.in.web.dto.CreateTransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TransactionControllerTest {

    @Mock
    TransactionManagementPort transactionManagementPort;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    TransactionsController transactionsController;

    @BeforeEach
    void setup(){
        openMocks(this);
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(transactionsController)
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    @Test
    void givenValidRequest_whenTransactionRequested_thenCreateTransactionAndReturn201() throws Exception {
        //given
        long accountId = 1L;
        int opTypeId = 1;
        long transactionId = 500L;
        BigDecimal amount = BigDecimal.valueOf(100.00);

        CreateTransactionRequest request = new CreateTransactionRequest(accountId, opTypeId, amount);

        TransactionResult mockTransaction = TransactionResult.builder()
                .transactionId(transactionId)
                .accountId(accountId)
                .amount(amount)
                .build();

        when(transactionManagementPort.saveTransactionAndDischargeDebitBalances(any(CreateTransactionCommand.class))).thenReturn(mockTransaction);
        // When-then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transaction_id").value(transactionId))
                .andExpect(jsonPath("$.amount").value(amount));

        ArgumentCaptor<CreateTransactionCommand> argumentCaptor = ArgumentCaptor.forClass(CreateTransactionCommand.class);
        verify(transactionManagementPort).saveTransactionAndDischargeDebitBalances(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().accountId()).isEqualTo(accountId);
        assertThat(argumentCaptor.getValue().amount()).isEqualTo(amount);
        assertThat(argumentCaptor.getValue().operationType().getId()).isEqualTo(opTypeId);
    }

    @Test
    void givenTransactionRequestWithInvalidOpTypeId_whenTransactionRequested_thenCreateTransactionAndReturn201() throws Exception {
        // 1. Given
        CreateTransactionRequest request = new CreateTransactionRequest(1L, 5, BigDecimal.valueOf(100.00));

        TransactionResult mockTransaction = TransactionResult.builder()
                .transactionId(500L)
                .accountId(1L)
                .amount(BigDecimal.valueOf(100.00))
                .build();

        when(transactionManagementPort.saveTransactionAndDischargeDebitBalances(any(CreateTransactionCommand.class)))
                .thenReturn(mockTransaction);

        // When - Then
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/transactions"))
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail").value("Invalid operation type id - 5"));

        verifyNoInteractions(transactionManagementPort);
    }

    @Test
    void givenMalformedJson_whenTransactionRequested_thenReturnBadRequest() throws Exception {
        String brokenJson = "{\"account_id\": 1, \"amount\": \"INVALID_NUMBER\" }";

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest());
    }
}


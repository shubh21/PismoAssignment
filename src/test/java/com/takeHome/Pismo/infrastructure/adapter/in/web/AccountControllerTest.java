package com.takeHome.Pismo.infrastructure.adapter.in.web;

import com.takeHome.Pismo.core.contract.input.CreateAccountCommand;
import com.takeHome.Pismo.core.contract.input.GetAccountByAccountIdQuery;
import com.takeHome.Pismo.core.contract.output.AccountResult;
import com.takeHome.Pismo.core.domain.port.in.AccountManagementPort;
import com.takeHome.Pismo.core.exception.AccountNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.stream.Stream;

import static com.takeHome.Pismo.core.Constants.INVALID_ACCOUNT_ID_MSG;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountsController.class)
public class AccountControllerTest {

    private static final long DOCUMENT_NUMBER = 12345678900L;
    private static final long ACCOUNT_ID = 1L;

    @MockitoBean
    AccountManagementPort accountManagementPort;

    @MockitoBean
    DataSource dataSource;

    @Autowired
    private MockMvc mockMvc;

    AccountsController accountsController;

    @BeforeEach
    void setup(){
        accountsController = new AccountsController(accountManagementPort);
    }

    @Test
    void givenDocumentNumber_whenCreateAccountRequested_thenCreateAccount() throws Exception {
        //given

        AccountResult accountResult = AccountResult.builder().accountId(ACCOUNT_ID).documentNumber(DOCUMENT_NUMBER).build();

        when(accountManagementPort.saveAccount(any(CreateAccountCommand.class))).thenReturn(accountResult);

        String json = String.format("{\"document_number\": %s}", DOCUMENT_NUMBER);

        //when-then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.account_id").isNumber())
                .andExpect(jsonPath("$.account_id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.document_number").value(DOCUMENT_NUMBER));

        ArgumentCaptor<CreateAccountCommand> captor = ArgumentCaptor.forClass(CreateAccountCommand.class);
        verify(accountManagementPort).saveAccount(captor.capture());
    }

    @ParameterizedTest(name = "Should return 400 when document number is invalid")
    @MethodSource("invalidDocumentProvider")
    void givenInvalidDocumentNumber_whenCreateAccountRequested_thenReturnBadRequest(
                                                                                    String jsonValue) throws Exception {
        //given
        String json = String.format("{\"document_number\": %s}", jsonValue);

        //when-then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed JSON request"))
                .andExpect(jsonPath("$.path").value("/accounts"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.detail").isString());

        verify(accountManagementPort, never()).saveAccount(any());
    }

    static Stream<Arguments> invalidDocumentProvider() {
        return Stream.of(
                Arguments.of( "null"),
                Arguments.of( "0"),
                Arguments.of( "-23")
        );
    }


    @Test
    void givenAccountId_whenRetrieveAccountRequested_thenReturnAccount() throws Exception {
        //Given
        long documentNumber = 12345678900L;
        long accountId = 1L;
        AccountResult accountResult = AccountResult.builder().accountId(accountId).documentNumber(documentNumber).build();

        when(accountManagementPort.retrieveAccount(any(GetAccountByAccountIdQuery.class))).thenReturn(accountResult);

        //When-then
        mockMvc.perform(get("/accounts/{account_id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").isNumber())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.document_number").value(documentNumber));

        ArgumentCaptor<GetAccountByAccountIdQuery> captor = ArgumentCaptor.forClass(GetAccountByAccountIdQuery.class);
        verify(accountManagementPort).retrieveAccount(captor.capture());
    }

    @Test
    void givenAccountId_whenRetrieveAccountRequested_thenReturnNoAccountFoundException() throws Exception {
        //Given
        long accountId = 1L;

        when(accountManagementPort.retrieveAccount(any(GetAccountByAccountIdQuery.class))).thenThrow(AccountNotFoundException.class);

        //When-then
        mockMvc.perform(get("/accounts/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.path").value("/accounts/1"))
                .andExpect(jsonPath("$.title").value("Account not found"))
                .andExpect(jsonPath("$.status").value(404));

        ArgumentCaptor<GetAccountByAccountIdQuery> captor = ArgumentCaptor.forClass(GetAccountByAccountIdQuery.class);
        verify(accountManagementPort).retrieveAccount(captor.capture());
    }

    @Test
    void givenZeroAccountId_whenRetrieveAccountRequested_thenThrowIllegalArgumentException() throws Exception {
        //Given
        long accountId = 0L;

        when(accountManagementPort.retrieveAccount(any(GetAccountByAccountIdQuery.class))).thenThrow(IllegalArgumentException.class);

        //When-then
        mockMvc.perform(get("/accounts/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.path").value("/accounts/0"))
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail").value(INVALID_ACCOUNT_ID_MSG.formatted(0L)))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(accountManagementPort);
    }
}

package com.takeHome.Pismo.core.domain.usecase;

import com.takeHome.Pismo.core.contract.input.CreateAccountCommand;
import com.takeHome.Pismo.core.contract.input.GetAccountByAccountIdQuery;
import com.takeHome.Pismo.core.contract.output.AccountResult;
import com.takeHome.Pismo.core.domain.model.Account;
import com.takeHome.Pismo.core.domain.port.out.AccountPersistencePort;
import com.takeHome.Pismo.core.exception.AccountNotFoundException;
import com.takeHome.Pismo.core.exception.DuplicateDocumentNumberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.takeHome.Pismo.core.Constants.ACCOUNT_NOT_FOUND_EXCEPTION_MSG;
import static com.takeHome.Pismo.core.Constants.DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG;
import static com.takeHome.Pismo.core.Constants.INVALID_DOCUMENT_NUMBER_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountManagementUseCaseTest {

    private static final long DOCUMENT_NUMBER = 123456789L;
    private static final long ACCOUNT_ID = 1L;

    @Mock
    private AccountPersistencePort accountPersistencePort;

    private AccountManagementUseCase accountManagementUseCase;

    @BeforeEach
    void setup(){
        accountManagementUseCase = new AccountManagementUseCase(accountPersistencePort);
    }

    @Test
    void givenValidCreateAccountCommand_whenSaveAccountCalled_thenAccountIsPersistedAndResultReturned() {
        // Given
        CreateAccountCommand command = new CreateAccountCommand(DOCUMENT_NUMBER);

        Account savedAccount = new Account(1L, DOCUMENT_NUMBER);

        when(accountPersistencePort.save(any(Account.class))).thenReturn(savedAccount);

        // When
        AccountResult result = accountManagementUseCase.saveAccount(command);

        // Then: verify the Account passed to persistence
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountPersistencePort).save(accountCaptor.capture());

        Account captured = accountCaptor.getValue();
        // accountId should be null before persistence
        assertThat(captured.accountId()).isNull();
        assertThat(captured.documentNumber()).isEqualTo(DOCUMENT_NUMBER);

        assertThat(result.accountId()).isEqualTo(savedAccount.accountId());
        assertThat(result.documentNumber()).isEqualTo(savedAccount.documentNumber());
    }

    @Test
    void givenInvalidDocumentNumberInCreateAccountCommand_whenSaveAccountCalled_thenIllegalArgumentExceptionThrownAndPersistenceNotInvoked() {
        // Given
        long invalidDocumentNumber = 0L;

        // When- Then
        assertThatThrownBy(() -> new CreateAccountCommand(invalidDocumentNumber))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_DOCUMENT_NUMBER_MSG.formatted(invalidDocumentNumber));

        verifyNoInteractions(accountPersistencePort);
    }

    @Test
    void givenDuplicateDocumentNumber_whenSaveAccountInUseCase_thenDuplicateDocumentNumberExceptionBubblesUp() {
        // Given
        CreateAccountCommand command = new CreateAccountCommand(DOCUMENT_NUMBER);

        // When
        when(accountPersistencePort.save(any(Account.class)))
                .thenThrow(new DuplicateDocumentNumberException(DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG.formatted(DOCUMENT_NUMBER)));

        // Then
        assertThatThrownBy(() -> accountManagementUseCase.saveAccount(command))
                .isInstanceOf(DuplicateDocumentNumberException.class)
                .hasMessage(DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG.formatted(DOCUMENT_NUMBER));

        verify(accountPersistencePort).save(any(Account.class));
    }


    @Test
    void givenExistingAccountId_whenRetrieveAccountCalled_thenAccountResultIsReturned() {
        // Given
        GetAccountByAccountIdQuery query = new GetAccountByAccountIdQuery(ACCOUNT_ID);

        Account account = new Account(ACCOUNT_ID, DOCUMENT_NUMBER);

        when(accountPersistencePort.retrieve(ACCOUNT_ID)).thenReturn(Optional.of(account));

        // When
        AccountResult result = accountManagementUseCase.retrieveAccount(query);

        // Then
        verify(accountPersistencePort).retrieve(ACCOUNT_ID);
        assertThat(result.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }

    @Test
    void givenNonExistingAccountId_whenRetrieveAccountCalled_thenAccountNotFoundExceptionThrown() {
        // Given
        long missingAccountId = 999L;
        GetAccountByAccountIdQuery query = new GetAccountByAccountIdQuery(missingAccountId);

        when(accountPersistencePort.retrieve(missingAccountId)).thenReturn(Optional.empty());

        // When- Then
        assertThatThrownBy(() -> accountManagementUseCase.retrieveAccount(query))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(ACCOUNT_NOT_FOUND_EXCEPTION_MSG.formatted(missingAccountId));

        verify(accountPersistencePort).retrieve(missingAccountId);
    }
}


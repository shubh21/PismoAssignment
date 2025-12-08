package com.takeHome.Pismo.core.domain.usecase;

import com.takeHome.Pismo.core.contract.input.CreateTransactionCommand;
import com.takeHome.Pismo.core.contract.output.TransactionResult;
import com.takeHome.Pismo.core.domain.model.OperationType;
import com.takeHome.Pismo.core.domain.model.Transaction;
import com.takeHome.Pismo.core.domain.port.out.TransactionPersistencePort;
import com.takeHome.Pismo.core.usecase.TransactionManagementUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static com.takeHome.Pismo.core.Constants.INVALID_AMOUNT_VALUE_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionManagementUseCaseTest {

    private static final long ACCOUNT_ID = 1L;
    private static final OperationType OPERATION_TYPE = OperationType.CASH_PURCHASE;
    private static final BigDecimal AMOUNT = BigDecimal.TEN;

    @Mock
    private TransactionPersistencePort transactionPersistencePort;

    private TransactionManagementUseCase transactionManagementUseCase;

    @BeforeEach
    void setup(){
        transactionManagementUseCase = new TransactionManagementUseCase(transactionPersistencePort);
    }

    @Test
    void givenValidCreateTransactionCommand_whenSaveTransactionCalled_thenTransactionIsPersistedAndResultReturned() {
        // Given
        CreateTransactionCommand command = new CreateTransactionCommand(ACCOUNT_ID, OperationType.CASH_PURCHASE, AMOUNT);

        LocalDateTime persistedEventDate = LocalDateTime.now();

        Transaction savedTransaction = new Transaction(
                1L,
                ACCOUNT_ID,
                OPERATION_TYPE.getId(),
                AMOUNT,
                persistedEventDate
        );

        when(transactionPersistencePort.save(any(Transaction.class))).thenReturn(savedTransaction);

        // When
        TransactionResult result = transactionManagementUseCase.saveTransaction(command);

        //verify passed values to persistence layers
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionPersistencePort).save(txCaptor.capture());

        Transaction captured = txCaptor.getValue();
        assertThat(captured.transactionId()).isNull();
        assertThat(captured.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(captured.operationTypeId()).isEqualTo(OPERATION_TYPE.getId());
        assertThat(captured.amount()).isEqualByComparingTo(AMOUNT.negate());
        assertThat(captured.eventDate()).isNotNull();

        //verify result
        assertThat(result.transactionId()).isEqualTo(savedTransaction.transactionId());
        assertThat(result.accountId()).isEqualTo(savedTransaction.accountId());
        assertThat(result.operationTypeId()).isEqualTo(savedTransaction.operationTypeId());
        assertThat(result.amount()).isEqualByComparingTo(savedTransaction.amount());
        assertThat(result.eventDate()).isEqualTo(savedTransaction.eventDate());
    }


    @Test
    void givenPersistencePortThrowsException_whenSaveTransactionCalled_thenExceptionIsPropagated() {
        // Given
        CreateTransactionCommand command = new CreateTransactionCommand(ACCOUNT_ID, OPERATION_TYPE, AMOUNT);

        RuntimeException persistenceException = new RuntimeException("DB is down");

        when(transactionPersistencePort.save(any(Transaction.class))).thenThrow(persistenceException);

        // When
        assertThatThrownBy(() -> transactionManagementUseCase.saveTransaction(command))
                .isSameAs(persistenceException);

        verify(transactionPersistencePort).save(any(Transaction.class));
    }


    @Test
    void givenNullAmountInCommand_whenSaveTransactionCalled_thenIllegalArgumentExceptionThrownAndPersistenceNotInvoked() {
        // Given
        BigDecimal amount = null;

        // When-Then
        assertThatThrownBy(() -> new CreateTransactionCommand(ACCOUNT_ID, OPERATION_TYPE, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(INVALID_AMOUNT_VALUE_MSG.formatted(amount));

        verifyNoInteractions(transactionPersistencePort);
    }

    @Test
    void givenInvalidOperationTypeIdInCommand_whenSaveTransactionCalled_thenIllegalArgumentExceptionThrownAndPersistenceNotInvoked() {
        // Given
        OperationType invalidOp = mock(OperationType.class);
        when(invalidOp.getId()).thenReturn(999);

        CreateTransactionCommand command = new CreateTransactionCommand(ACCOUNT_ID, invalidOp, BigDecimal.TEN);

        // When / Then
        assertThatThrownBy(() -> transactionManagementUseCase.saveTransaction(command))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(transactionPersistencePort);
    }

    @Test
    void givenSavedTransaction_whenMappedToResult_thenAllFieldsMatch() {

        // Given
        LocalDateTime eventDate = LocalDateTime.now();
        CreateTransactionCommand command = new CreateTransactionCommand(ACCOUNT_ID, OPERATION_TYPE, AMOUNT);
        Transaction saved = new Transaction(42L, ACCOUNT_ID, OPERATION_TYPE.getId(), AMOUNT, eventDate);
        when(transactionPersistencePort.save(any(Transaction.class))).thenReturn(saved);

        // When
        TransactionResult result = transactionManagementUseCase.saveTransaction(command);

        // Then
        assertThat(result.transactionId()).isEqualTo(saved.transactionId());
        assertThat(result.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.operationTypeId()).isEqualTo(OPERATION_TYPE.getId());
        assertThat(result.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(result.eventDate()).isEqualTo(eventDate);
    }
}
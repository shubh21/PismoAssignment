package com.takeHome.Pismo.core.domain.usecase;

import com.takeHome.Pismo.core.contract.input.CreateTransactionCommand;
import com.takeHome.Pismo.core.contract.output.TransactionResult;
import com.takeHome.Pismo.core.domain.model.BalanceBearingTransaction;
import com.takeHome.Pismo.core.domain.model.ExecutableTransaction;
import com.takeHome.Pismo.core.domain.model.OperationType;
import com.takeHome.Pismo.core.domain.model.Transaction;
import com.takeHome.Pismo.core.domain.model.TransactionDischargeResult;
import com.takeHome.Pismo.core.domain.port.in.BalanceDischargePort;
import com.takeHome.Pismo.core.domain.port.out.TransactionPersistencePort;
import com.takeHome.Pismo.core.usecase.TransactionManagementUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static com.takeHome.Pismo.core.Constants.INVALID_AMOUNT_VALUE_MSG;
import static com.takeHome.Pismo.core.domain.model.OperationType.CASH_PURCHASE;
import static com.takeHome.Pismo.core.domain.model.OperationType.INSTALLMENT_PURCHASE;
import static com.takeHome.Pismo.core.domain.model.OperationType.PAYMENT;
import static com.takeHome.Pismo.core.domain.model.OperationType.WITHDRAWAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionManagementUseCaseTest {

    private static final long ACCOUNT_ID = 1L;
    private static final OperationType OPERATION_TYPE = CASH_PURCHASE;
    private static final BigDecimal AMOUNT = BigDecimal.TEN;

    @Mock
    private TransactionPersistencePort transactionPersistencePort;

    @Mock
    private BalanceDischargePort balanceDischargePort;

    private TransactionManagementUseCase transactionManagementUseCase;

    @BeforeEach
    void setup() {
        transactionManagementUseCase = new TransactionManagementUseCase(transactionPersistencePort, balanceDischargePort);
    }

    @Test
    void givenValidTransactionCommandForPaymentOpType_whenSaveTransactionAndDischargeDebitBalances_thenTransactionIsPersistedWithCorrectAmount() {
        // Given
        CreateTransactionCommand command = new CreateTransactionCommand(ACCOUNT_ID, PAYMENT, AMOUNT);

        LocalDateTime persistedEventDate = LocalDateTime.now();

        BalanceBearingTransaction savedTransaction = ExecutableTransaction.from(
                new Transaction(1L, ACCOUNT_ID, PAYMENT.getId(), AMOUNT, persistedEventDate));

        List<BalanceBearingTransaction> newList = new ArrayList<>();

        when(transactionPersistencePort.save(any(BalanceBearingTransaction.class))).thenReturn(savedTransaction);
        when(transactionPersistencePort.fetchDebitTransactions(ACCOUNT_ID)).thenReturn(newList);

        // When
        TransactionResult result = transactionManagementUseCase.saveTransactionAndDischargeDebitBalances(command);

        //verify passed values to persistence layers
        ArgumentCaptor<BalanceBearingTransaction> txCaptor = ArgumentCaptor.forClass(BalanceBearingTransaction.class);
        verify(transactionPersistencePort).save(txCaptor.capture());

        BalanceBearingTransaction captured = txCaptor.getValue();
        assertThat(captured.transaction().transactionId()).isNull();
        assertThat(captured.transaction().accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(captured.transaction().operationTypeId()).isEqualTo(PAYMENT.getId());
        assertThat(captured.transaction().amount()).isEqualByComparingTo(AMOUNT);
        assertThat(captured.transaction().eventDate()).isNotNull();
        verify(transactionPersistencePort).fetchDebitTransactions(ACCOUNT_ID);

        verify(balanceDischargePort, never()).discharge(savedTransaction, newList);

        verify(transactionPersistencePort, never()).updateTransaction(savedTransaction);

        //verify result
        assertThat(result.transactionId()).isEqualTo(savedTransaction.transaction().transactionId());
        assertThat(result.accountId()).isEqualTo(savedTransaction.transaction().accountId());
        assertThat(result.operationTypeId()).isEqualTo(savedTransaction.transaction().operationTypeId());
        assertThat(result.amount()).isEqualByComparingTo(savedTransaction.transaction().amount());
        assertThat(result.eventDate()).isEqualTo(savedTransaction.transaction().eventDate());
    }

    @ParameterizedTest
    @EnumSource(names = {"CASH_PURCHASE", "INSTALLMENT_PURCHASE", "WITHDRAWAL"})
    void givenValidCommandForPurchaseAndWithdrawalOpTypes_whenSaveTransactionAndDischargeDebitBalances_thenSaveAndDischargeTransactionIsPersistedWithSignedValues(OperationType operationType) {
        // Given
        CreateTransactionCommand command = new CreateTransactionCommand(ACCOUNT_ID, operationType, AMOUNT);

        LocalDateTime persistedEventDate = LocalDateTime.now();

        Transaction transaction = new Transaction(1L, ACCOUNT_ID, operationType.getId(), AMOUNT, persistedEventDate);
        BalanceBearingTransaction savedTransaction = ExecutableTransaction.from(transaction);
        when(transactionPersistencePort.save(any(BalanceBearingTransaction.class))).thenReturn(savedTransaction);

        // When
        TransactionResult result = transactionManagementUseCase.saveTransactionAndDischargeDebitBalances(command);

        ArgumentCaptor<BalanceBearingTransaction> txCaptor = ArgumentCaptor.forClass(BalanceBearingTransaction.class);
        verify(transactionPersistencePort).save(txCaptor.capture());

        BalanceBearingTransaction captured = txCaptor.getValue();
        assertThat(captured.transaction().transactionId()).isNull();
        assertThat(captured.transaction().accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(captured.transaction().operationTypeId()).isEqualTo(operationType.getId());
        assertThat(captured.transaction().amount()).isEqualByComparingTo(AMOUNT.negate());
        assertThat(captured.transaction().eventDate()).isNotNull();
        verify(transactionPersistencePort, never()).fetchDebitTransactions(ACCOUNT_ID);
        verify(transactionPersistencePort, never()).updateTransaction(savedTransaction);

        verifyNoInteractions(balanceDischargePort);

        assertThat(result.transactionId()).isEqualTo(savedTransaction.transaction().transactionId());
        assertThat(result.accountId()).isEqualTo(savedTransaction.transaction().accountId());
        assertThat(result.operationTypeId()).isEqualTo(savedTransaction.transaction().operationTypeId());
        assertThat(result.amount()).isEqualByComparingTo(savedTransaction.transaction().amount());
        assertThat(result.eventDate()).isEqualTo(savedTransaction.transaction().eventDate());
    }


    @Test
    void givenPersistencePortThrowsException_whenSaveTransactionAndDischargeDebitBalancesCalled_thenExceptionIsPropagated() {
        // Given
        CreateTransactionCommand command = new CreateTransactionCommand(ACCOUNT_ID, OPERATION_TYPE, AMOUNT);

        RuntimeException persistenceException = new RuntimeException("DB is down");

        when(transactionPersistencePort.save(any(BalanceBearingTransaction.class))).thenThrow(persistenceException);

        // When
        assertThatThrownBy(() -> transactionManagementUseCase.saveTransactionAndDischargeDebitBalances(command))
                .isSameAs(persistenceException);

        verify(transactionPersistencePort).save(any(BalanceBearingTransaction.class));
        verify(transactionPersistencePort, never()).fetchDebitTransactions(ACCOUNT_ID);
        verify(transactionPersistencePort, never()).updateTransaction(any(BalanceBearingTransaction.class));
        verifyNoInteractions(balanceDischargePort);

    }


    @Test
    void givenNullAmountInCommand_whenSaveTransactionAndDischargeDebitBalancesCalled_thenIllegalArgumentExceptionThrownAndPersistenceNotInvokedDebit() {
        // Given
        BigDecimal amount = null;

        // When-Then
        assertThatThrownBy(() -> new CreateTransactionCommand(ACCOUNT_ID, OPERATION_TYPE, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(INVALID_AMOUNT_VALUE_MSG.formatted(amount));

        verifyNoInteractions(transactionPersistencePort, balanceDischargePort);
    }

    @Test
    void givenInvalidOperationTypeIdInCommand_whenSaveTransactionAndDischargeDebitBalancesCalled_thenIllegalArgumentExceptionThrownAndPersistenceNotInvokedDebit() {
        // Given
        OperationType invalidOp = mock(OperationType.class);
        when(invalidOp.getId()).thenReturn(999);

        CreateTransactionCommand command = new CreateTransactionCommand(ACCOUNT_ID, invalidOp, BigDecimal.TEN);

        // When / Then
        assertThatThrownBy(() -> transactionManagementUseCase.saveTransactionAndDischargeDebitBalances(command))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(transactionPersistencePort, balanceDischargePort);
    }

    @Test
    void givenSavedTransaction_whenMappedToResult_thenAllFieldsMatch() {

        // Given
        LocalDateTime eventDate = LocalDateTime.now();
        CreateTransactionCommand command = new CreateTransactionCommand(ACCOUNT_ID, OPERATION_TYPE, AMOUNT);
        Transaction saved = new Transaction(42L, ACCOUNT_ID, OPERATION_TYPE.getId(), AMOUNT, eventDate);

        when(transactionPersistencePort.save(any(BalanceBearingTransaction.class))).thenReturn(ExecutableTransaction.from(saved));

        // When
        TransactionResult result = transactionManagementUseCase.saveTransactionAndDischargeDebitBalances(command);

        // Then
        assertThat(result.transactionId()).isEqualTo(saved.transactionId());
        assertThat(result.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.operationTypeId()).isEqualTo(OPERATION_TYPE.getId());
        assertThat(result.amount()).isEqualByComparingTo(AMOUNT.negate());
        assertThat(result.eventDate()).isEqualTo(eventDate);
        verify(transactionPersistencePort).save(any(BalanceBearingTransaction.class));
        verify(transactionPersistencePort, never()).fetchDebitTransactions(ACCOUNT_ID);
        verify(transactionPersistencePort, never()).updateTransaction(any(BalanceBearingTransaction.class));
        verifyNoInteractions(balanceDischargePort);
    }


    @Test
    void givenPaymentCommand_whenSaved_thenDebitTransactionsAreDischarged() {
        // Given
        BigDecimal value1 = BigDecimal.valueOf(50);
        BigDecimal value2 = BigDecimal.valueOf(23.5);
        BigDecimal value3 = BigDecimal.valueOf(18.7);
        BigDecimal value4 = BigDecimal.valueOf(60);

        CreateTransactionCommand command1 = new CreateTransactionCommand(ACCOUNT_ID, CASH_PURCHASE, value1);
        CreateTransactionCommand command2= new CreateTransactionCommand(ACCOUNT_ID, INSTALLMENT_PURCHASE, value2);
        CreateTransactionCommand command3 = new CreateTransactionCommand(ACCOUNT_ID, WITHDRAWAL, value3);
        CreateTransactionCommand command4 = new CreateTransactionCommand(ACCOUNT_ID, PAYMENT, value4);
        List<CreateTransactionCommand> list = List.of(command1,command2, command3, command4);

        BalanceBearingTransaction purchaseTx1 = debitTx(1L, CASH_PURCHASE, value1);
        BalanceBearingTransaction purchaseTx2 = debitTx(2L, INSTALLMENT_PURCHASE, value2);
        BalanceBearingTransaction withdrawalTx = debitTx(3L, WITHDRAWAL, value3);
        BalanceBearingTransaction savedPaymentTx = paymentTx(value4);

        BalanceBearingTransaction dischargedPaymentTx = fromTransaction(savedPaymentTx, BigDecimal.ZERO);
        BalanceBearingTransaction dischargedPurchaseTx1 = fromTransaction(purchaseTx1, BigDecimal.ZERO);
        BalanceBearingTransaction dischargedPurchaseTx2 = fromTransaction(purchaseTx2, BigDecimal.valueOf(13.5).negate());
        BalanceBearingTransaction dischargedWithdrawalTx = fromTransaction(withdrawalTx, value3.negate());

        TransactionDischargeResult transactionDischargeResult = new TransactionDischargeResult(dischargedPaymentTx, List.of( dischargedPurchaseTx1, dischargedPurchaseTx2, dischargedWithdrawalTx));

        when(transactionPersistencePort.save(any(BalanceBearingTransaction.class))).thenReturn(savedPaymentTx);
        when(transactionPersistencePort.fetchDebitTransactions(ACCOUNT_ID)).thenReturn(List.of( purchaseTx1, purchaseTx2, withdrawalTx));
        when(balanceDischargePort.discharge(savedPaymentTx, List.of( purchaseTx1, purchaseTx2, withdrawalTx)))
                .thenReturn(transactionDischargeResult);

        // When
        for(CreateTransactionCommand command: list) {
            TransactionResult result = transactionManagementUseCase.saveTransactionAndDischargeDebitBalances(command);

            // Result sanity
            assertThat(result.accountId()).isEqualTo(ACCOUNT_ID);
            assertThat(result.operationTypeId()).isEqualTo(PAYMENT.getId());
        }
        // Then
        ArgumentCaptor<BalanceBearingTransaction> savedCaptor = ArgumentCaptor.forClass(BalanceBearingTransaction.class);

        verify(transactionPersistencePort, times(4)).save(savedCaptor.capture());
        List<BalanceBearingTransaction> saved = savedCaptor.getAllValues();
        assertThat(saved).isNotEmpty();
        assertThat(saved).hasSize(4);

        List<BalanceBearingTransaction> captured = savedCaptor.getAllValues();

        assertThat(captured)
                .allSatisfy(txn -> assertThat(txn.transaction().accountId()).isEqualTo(ACCOUNT_ID));
        assertThat(captured)
                .allSatisfy(txn -> assertThat(txn.transaction().transactionId()).isNull());
        assertThat(captured)
                .allSatisfy(txn -> assertThat(txn.transaction().eventDate()).isNotNull());

        assertThat(captured.getFirst().transaction().amount())
                .isEqualByComparingTo(purchaseTx1.transaction().amount());
        assertThat(captured.get(1).transaction().amount())
                .isEqualByComparingTo(purchaseTx2.transaction().amount());
        assertThat(captured.get(2).transaction().amount())
                .isEqualByComparingTo(withdrawalTx.transaction().amount());
        assertThat(captured.getLast().transaction().amount())
                .isEqualByComparingTo(savedPaymentTx.transaction().amount());

        assertThat(captured.getFirst().balance()).isEqualByComparingTo(purchaseTx1.balance());
        assertThat(captured.get(1).balance()).isEqualByComparingTo(purchaseTx2.balance());
        assertThat(captured.get(2).balance()).isEqualByComparingTo(withdrawalTx.balance());
        assertThat(captured.getLast().balance()).isEqualByComparingTo(savedPaymentTx.balance());

        assertThat(captured.getLast().balance()).isEqualByComparingTo(BigDecimal.valueOf(60));

        verify(transactionPersistencePort).fetchDebitTransactions(ACCOUNT_ID);
        verify(balanceDischargePort).discharge(savedPaymentTx, List.of( purchaseTx1, purchaseTx2, withdrawalTx));

        ArgumentCaptor<BalanceBearingTransaction> updateCaptor = ArgumentCaptor.forClass(BalanceBearingTransaction.class);

        verify(transactionPersistencePort, times(4)).updateTransaction(updateCaptor.capture());

        List<BalanceBearingTransaction> updated = updateCaptor.getAllValues();

        assertThat(updated).hasSize(4);
        assertThat(updated)
                .allSatisfy(txn -> assertThat(txn.transaction().accountId()).isEqualTo(ACCOUNT_ID));
        updated.forEach(txn ->
                assertThat(txn.balance()).isNotNull());

        assertThat(updated.getFirst().transaction().amount())
                .isEqualByComparingTo(purchaseTx1.transaction().amount());
        assertThat(updated.get(1).transaction().amount())
                .isEqualByComparingTo(purchaseTx2.transaction().amount());
        assertThat(updated.get(2).transaction().amount())
                .isEqualByComparingTo(withdrawalTx.transaction().amount());
        assertThat(updated.getLast().transaction().amount())
                .isEqualByComparingTo(savedPaymentTx.transaction().amount());

        assertThat(updated.getFirst().balance()).isEqualByComparingTo(dischargedPurchaseTx1.balance());
        assertThat(updated.get(1).balance()).isEqualByComparingTo(dischargedPurchaseTx2.balance());
        assertThat(updated.get(2).balance()).isEqualByComparingTo(dischargedWithdrawalTx.balance());
        assertThat(updated.getLast().balance()).isEqualByComparingTo(dischargedPaymentTx.balance());
    }

    private BalanceBearingTransaction fromTransaction(BalanceBearingTransaction savedPaymentTx, BigDecimal amount) {
        return ExecutableTransaction.from(savedPaymentTx.transaction(), amount);
    }

    private BalanceBearingTransaction debitTx(long id, OperationType type, BigDecimal amount) {
        return ExecutableTransaction.from(new Transaction(id, ACCOUNT_ID, type.getId(), amount, LocalDateTime.now()));
    }

    private BalanceBearingTransaction paymentTx(BigDecimal amount) {
        return ExecutableTransaction.from(new Transaction(4L, ACCOUNT_ID, PAYMENT.getId(), amount, LocalDateTime.now()));
    }
}
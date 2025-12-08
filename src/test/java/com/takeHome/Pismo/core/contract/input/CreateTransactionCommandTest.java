package com.takeHome.Pismo.core.contract.input;

import com.takeHome.Pismo.core.Constants;
import com.takeHome.Pismo.core.domain.model.OperationType;
import com.takeHome.Pismo.infrastructure.adapter.in.web.dto.CreateTransactionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import static com.takeHome.Pismo.core.Constants.INVALID_ACCOUNT_ID_MSG;
import static com.takeHome.Pismo.core.Constants.INVALID_AMOUNT_VALUE_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class CreateTransactionCommandTest {

    private static final OperationType OPERATION_TYPE = OperationType.CASH_PURCHASE;
    private static final long ACCOUNT_ID = 123L;
    private static final BigDecimal AMOUNT = BigDecimal.TEN;

    @Test
    void givenValidParameters_whenCommandIsCreated_thenParametersAreStored(){

        CreateTransactionCommand command = new CreateTransactionCommand(ACCOUNT_ID, OPERATION_TYPE, AMOUNT);

        assertThat(command.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(command.operationType()).isEqualTo(OPERATION_TYPE);
        assertThat(command.amount()).isEqualByComparingTo(AMOUNT);
    }


    @ParameterizedTest
    @ValueSource(longs = {-1, 0, Long.MIN_VALUE})
    void givenInvalidAccountId_whenCommandIsCreated_thenIllegalArgumentExceptionIsThrown(long accountId){

        assertThatThrownBy(() -> new CreateTransactionCommand(accountId, OPERATION_TYPE, AMOUNT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(INVALID_ACCOUNT_ID_MSG, accountId));
    }

    @Test
    void givenNullAmount_whenCommandIsCreated_thenIllegalArgumentExceptionIsThrown(){
        BigDecimal amount = null;
        assertThatThrownBy(() -> new CreateTransactionCommand(ACCOUNT_ID, OPERATION_TYPE, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(INVALID_AMOUNT_VALUE_MSG.formatted(amount));
    }

    @Test
    void givenNegativeAmount_whenCommandIsCreated_thenIllegalArgumentExceptionIsThrown(){
        BigDecimal amount = BigDecimal.valueOf(-2L);
        assertThatThrownBy(() -> new CreateTransactionCommand(ACCOUNT_ID, OPERATION_TYPE, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(INVALID_AMOUNT_VALUE_MSG.formatted(amount));
    }

    @Test
    void givenZeroValueAmount_whenCommandIsCreated_thenParametersAreStored(){
        BigDecimal amount = BigDecimal.ZERO;
        CreateTransactionCommand command = new CreateTransactionCommand(ACCOUNT_ID, OPERATION_TYPE, amount);

        assertThat(command.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(command.operationType()).isEqualTo(OPERATION_TYPE);
        assertThat(command.amount()).isEqualByComparingTo(amount);
    }

    @ParameterizedTest
    @EnumSource(OperationType.class)
    void givenEachOperationType_whenCommandIsCreated_thenCorrectValueIsStored(OperationType operationType) {

        // When
        CreateTransactionCommand command = new CreateTransactionCommand(ACCOUNT_ID, operationType, AMOUNT);

        // Then
        assertThat(command.operationType()).isEqualTo(operationType);
        assertThat(command.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(command.amount()).isEqualTo(AMOUNT);
    }

    @Test
    void givenValidParameters_whenFromRequestIsInvoked_thenParametersAreStored(){
        CreateTransactionRequest request = new CreateTransactionRequest(
                ACCOUNT_ID,
                OPERATION_TYPE.getId(),
                AMOUNT
        );

        CreateTransactionCommand command = CreateTransactionCommand.fromRequest(request);

        assertThat(command.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(command.operationType()).isEqualTo(OPERATION_TYPE);
        assertThat(command.amount()).isEqualByComparingTo(AMOUNT);
    }


    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 9999})
    void givenInvalidOperationTypeId_whenFromRequestIsInvoked_thenIllegalArgumentExceptionIsThrown(int invalidOperationTypeId) {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest(
                ACCOUNT_ID,
                invalidOperationTypeId,
               AMOUNT
        );

        assertThatThrownBy(() -> CreateTransactionCommand.fromRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(Constants.INVALID_OPERATION_ID_MSG,invalidOperationTypeId));
    }
}

package com.takeHome.Pismo.core.contract.input;

import com.takeHome.Pismo.core.domain.model.OperationType;
import com.takeHome.Pismo.infrastructure.adapter.in.web.dto.CreateTransactionRequest;
import java.math.BigDecimal;
import java.util.Objects;
import static com.takeHome.Pismo.core.Constants.INVALID_ACCOUNT_ID_MSG;
import static com.takeHome.Pismo.core.Constants.INVALID_AMOUNT_VALUE_MSG;

public record CreateTransactionCommand(long accountId,
                                       OperationType operationType,
                                       BigDecimal amount) {

    public CreateTransactionCommand {
        validateAccountId(accountId);
        validateAmount(amount);
    }

    public static CreateTransactionCommand fromRequest(CreateTransactionRequest request) {
        validateAccountId(request.accountId());
        validateAmount(request.amount());

        OperationType type = OperationType.fromId(request.operationTypeId());

        return new CreateTransactionCommand(request.accountId(), type, request.amount());
    }

    private static void validateAccountId(long accountId){
        if(accountId<=0){
            throw new IllegalArgumentException(INVALID_ACCOUNT_ID_MSG.formatted(accountId));
        }
    }

    private static void validateAmount(BigDecimal amount){
        if(Objects.isNull(amount) || amount.compareTo(BigDecimal.ZERO)<0){
            throw new IllegalArgumentException(INVALID_AMOUNT_VALUE_MSG.formatted(amount));
        }
    }
}

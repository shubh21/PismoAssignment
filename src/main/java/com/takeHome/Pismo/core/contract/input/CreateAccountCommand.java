package com.takeHome.Pismo.core.contract.input;

import static com.takeHome.Pismo.core.Constants.INVALID_DOCUMENT_NUMBER_MSG;

public record CreateAccountCommand(long documentNumber) {

    public CreateAccountCommand {

        if (documentNumber <= 0) {
            throw new IllegalArgumentException(INVALID_DOCUMENT_NUMBER_MSG.formatted(documentNumber));
        }
    }
}

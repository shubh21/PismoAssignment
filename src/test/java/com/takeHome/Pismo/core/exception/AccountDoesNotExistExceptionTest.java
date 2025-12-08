package com.takeHome.Pismo.core.exception;

import org.junit.jupiter.api.Test;

import static com.takeHome.Pismo.core.Constants.ACCOUNT_NOT_EXISTS_EXCEPTION_MSG;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountDoesNotExistExceptionTest {

    @Test
    void givenMessage_whenConstructingAccountDoesNotExistException_thenMessageIsStoredAndTypeIsCorrect() {
        // Given
        String message = ACCOUNT_NOT_EXISTS_EXCEPTION_MSG.formatted(123);

        // When
        AccountDoesNotExistException ex = new AccountDoesNotExistException(message);

        // Then
        assertThat(ex)
                .isInstanceOf(AccountDoesNotExistException.class)
                .isInstanceOf(DomainException.class)
                .isInstanceOf(RuntimeException.class);

        assertThat(ex.getMessage()).isEqualTo(message);
    }
}

package com.takeHome.Pismo.core.exception;

import org.junit.jupiter.api.Test;

import static com.takeHome.Pismo.core.Constants.ACCOUNT_NOT_FOUND_EXCEPTION_MSG;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountNotFoundExceptionTest {

    @Test
    void givenMessage_whenConstructingException_thenMessageIsStoredAndTypeIsCorrect() {
        // Given
        String message = ACCOUNT_NOT_FOUND_EXCEPTION_MSG.formatted(123);

        // When
        AccountNotFoundException ex = new AccountNotFoundException(message);

        // Then
        assertThat(ex)
                .isInstanceOf(AccountNotFoundException.class)
                .isInstanceOf(DomainException.class)
                .isInstanceOf(RuntimeException.class);

        assertThat(ex.getMessage()).isEqualTo(message);
    }

}

package com.takeHome.Pismo.core.contract.input;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountCommandTest {

    @ParameterizedTest
    @ValueSource(longs = {10L, 0L, -1L, -123456789L, Long.MAX_VALUE, Long.MIN_VALUE})
    void givenDocumentNumber_whenCommandCreated_thenDocumentNumberIsStored() {
        // Given
        long documentNumber = 1234567890L;

        // When
        CreateAccountCommand command = new CreateAccountCommand(documentNumber);

        // Then
        assertThat(command.documentNumber()).isEqualTo(documentNumber);
    }
}

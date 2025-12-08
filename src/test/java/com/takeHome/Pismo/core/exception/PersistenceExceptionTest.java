package com.takeHome.Pismo.core.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PersistenceExceptionTest {

    @Test
    void givenMessage_whenConstructingPersistenceException_thenMessageIsStoredCorrectly() {
        // Given
        String message = "Database failure occurred";

        // When
        PersistenceException ex = new PersistenceException(message);

        // Then
        assertThat(ex).isInstanceOf(PersistenceException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo(message);
    }
}

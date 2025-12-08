package com.takeHome.Pismo.core.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainExceptionTest {

    static class TestDomainException extends DomainException {
        public TestDomainException(String message) {
            super(message);
        }
    }

    @Test
    void givenMessage_whenConstructingDomainExceptionSubclass_thenMessageIsStoredCorrectly() {
        // Given
        String message = "Test domain exception message";

        // When
        DomainException ex = new TestDomainException(message);

        // Then
        assertThat(ex).isInstanceOf(DomainException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo(message);
    }

}

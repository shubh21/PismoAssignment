package com.takeHome.Pismo.core.exception;

import org.junit.jupiter.api.Test;

import static com.takeHome.Pismo.core.Constants.DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG;
import static org.assertj.core.api.Assertions.assertThat;

public class DuplicateDocumentNumberExceptionTest {

    @Test
    void givenMessage_whenConstructingDuplicateDocumentNumberException_thenMessageIsStoredAndTypeIsCorrect() {
        // Given
        String message = DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG.formatted(123);

        // When
        DuplicateDocumentNumberException ex = new DuplicateDocumentNumberException(message);

        // Then
        assertThat(ex)
                .isInstanceOf(DuplicateDocumentNumberException.class)
                .isInstanceOf(DomainException.class)
                .isInstanceOf(RuntimeException.class);

        assertThat(ex.getMessage()).isEqualTo(message);
    }
}

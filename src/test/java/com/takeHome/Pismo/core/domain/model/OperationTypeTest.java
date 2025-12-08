package com.takeHome.Pismo.core.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static com.takeHome.Pismo.core.Constants.INVALID_OPERATION_ID_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationTypeTest {

    @Test
    void givenValidId_whenFromIdCalled_thenCorrectEnumReturned() {
        // Given
        int id = 1;

        // When
        OperationType type = OperationType.fromId(id);

        // Then
        assertThat(type).isEqualTo(OperationType.CASH_PURCHASE);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void givenEachValidId_whenFromIdCalled_thenCorrectEnumMapped(int id) {
        // When
        OperationType type = OperationType.fromId(id);

        // Then
        assertThat(type.getId()).isEqualTo(id);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 5, 99, Integer.MAX_VALUE})
    void givenInvalidId_whenFromIdCalled_thenIllegalArgumentExceptionThrown(int id) {
        // When - Then
        assertThatThrownBy(() -> OperationType.fromId(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(INVALID_OPERATION_ID_MSG.formatted(id));
    }

    @Test
    void givenEnumValues_whenGetIdCalled_thenIdIsCorrectForEachEnum() {
        // When- Then
        assertThat(OperationType.CASH_PURCHASE.getId()).isEqualTo(1);
        assertThat(OperationType.INSTALLMENT_PURCHASE.getId()).isEqualTo(2);
        assertThat(OperationType.WITHDRAWAL.getId()).isEqualTo(3);
        assertThat(OperationType.PAYMENT.getId()).isEqualTo(4);
    }

    @Test
    void givenEnumId_whenMappedBackAndForth_thenSymmetryIsMaintained() {

        for (OperationType type : OperationType.values()) {
            // When
            OperationType result = OperationType.fromId(type.getId());

            // Then
            assertThat(result).isEqualTo(type);
        }
    }

    @Test
    void givenEnumDefinition_whenCheckingCount_thenEnumCountIsFour() {
        assertThat(OperationType.values()).hasSize(4);
    }
}
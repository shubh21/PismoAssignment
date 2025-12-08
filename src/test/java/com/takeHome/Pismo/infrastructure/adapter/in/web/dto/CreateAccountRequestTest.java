package com.takeHome.Pismo.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.takeHome.Pismo.core.Constants.INVALID_DOCUMENT_NUMBER_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateAccountRequestTest {

    private static final long DOCUMENT_NUMBER = 123456789L;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup(){
        objectMapper = new ObjectMapper();
    }


    @Test
    void givenValidDocumentNumber_whenConstructed_thenFieldIsSetCorrectly() {

        // When
        CreateAccountRequest request = new CreateAccountRequest(DOCUMENT_NUMBER);

        // Then
        assertThat(request.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 9L, 123456L, Long.MAX_VALUE})
    void givenVariousValidDocumentNumbers_whenConstructed_thenFieldIsSetCorrectly(long documentNumber) {
        // When
        CreateAccountRequest request = new CreateAccountRequest(documentNumber);

        // Then
        assertThat(request.documentNumber()).isEqualTo(documentNumber);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -123456789L})
    void givenZeroOrNegativeDocumentNumber_whenConstructed_thenIllegalArgumentExceptionThrown(long invalidDocumentNumber) {
        // When / Then
        assertThatThrownBy(() -> new CreateAccountRequest(invalidDocumentNumber))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_DOCUMENT_NUMBER_MSG.formatted(invalidDocumentNumber));
    }

    @Test
    void givenNullDocumentNumber_whenConstructed_thenExceptionIsThrownFromFormatting() {

        Long documentNumber = null;
        assertThatThrownBy(() -> new CreateAccountRequest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_DOCUMENT_NUMBER_MSG.formatted(documentNumber));
    }

    @Test
    void givenCreateAccountRequest_whenSerializedToJson_thenUsesDocumentNumberSnakeCaseProperty() throws Exception {
        // Given
        CreateAccountRequest request = new CreateAccountRequest(DOCUMENT_NUMBER);

        // When
        String json = objectMapper.writeValueAsString(request);
        JsonNode node = objectMapper.readTree(json);

        // Then
        assertThat(node.get("document_number").asLong()).isEqualTo(DOCUMENT_NUMBER);
        assertThat(node.has("documentNumber")).isFalse();
    }

    @Test
    void givenJsonWithDocumentNumber_whenDeserialized_thenCreateAccountRequestIsPopulatedCorrectly() throws Exception {
        // Given
        String json = """
            {
              "document_number": 987654321
            }
            """;

        // When
        CreateAccountRequest request = objectMapper.readValue(json, CreateAccountRequest.class);

        // Then
        assertThat(request.documentNumber()).isEqualTo(987654321L);
    }
}

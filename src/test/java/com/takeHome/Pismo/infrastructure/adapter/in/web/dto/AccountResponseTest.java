package com.takeHome.Pismo.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

class AccountResponseTest {

    private static final long ACCOUNT_ID = 1L;
    private static final long DOCUMENT_NUMBER = 123456789L;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup(){
        objectMapper = new ObjectMapper();
    }

    @Test
    void givenValidValues_whenConstructorCalled_thenFieldsAreSetCorrectly() {

        // When
        AccountResponse response = new AccountResponse(ACCOUNT_ID, DOCUMENT_NUMBER);

        // Then
        assertThat(response.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }

    @Test
    void givenValidValues_whenBuilderUsed_thenAccountResponseIsCreatedCorrectly() {

        // When
        AccountResponse response = AccountResponse.builder()
                .accountId(ACCOUNT_ID)
                .documentNumber(DOCUMENT_NUMBER)
                .build();

        // Then
        assertThat(response.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 100",
            "2, 200",
            "-1, 1",
            "1, -1",
            "-12345, -67890",
            "9999, 123456789",
            "42, 42424242"
    })
    void givenMultiplePairsValues_whenBuilderUsed_thenAccountResponseIsCreated(long accountId, long documentNumber) {
        // When
        AccountResponse response = AccountResponse.builder()
                .accountId(accountId)
                .documentNumber(documentNumber)
                .build();

        // Then
        assertThat(response.accountId()).isEqualTo(accountId);
        assertThat(response.documentNumber()).isEqualTo(documentNumber);
    }

    @Test
    void givenOnlyAccountIdSet_whenBuilderUsed_thenDocumentNumberDefaultsToZero() {


        // When
        AccountResponse response = AccountResponse.builder()
                .accountId(ACCOUNT_ID)
                .build();

        // Then
        assertThat(response.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.documentNumber()).isZero();
    }

    @Test
    void givenOnlyDocumentNumberSet_whenBuilderUsed_thenAccountIdDefaultsToZero() {

        // When
        AccountResponse response = AccountResponse.builder().documentNumber(DOCUMENT_NUMBER).build();

        // Then
        assertThat(response.documentNumber()).isEqualTo(DOCUMENT_NUMBER);
        assertThat(response.accountId()).isZero();
    }

    @Test
    void givenFieldsSetInDifferentOrder_whenBuilderUsed_thenResultIsSame() {
        // Given
        long accountId = 10L;
        long documentNumber = 123456789L;

        // When
        AccountResponse responseOrder1 = AccountResponse.builder()
                .accountId(accountId)
                .documentNumber(documentNumber)
                .build();

        AccountResponse responseOrder2 = AccountResponse.builder()
                .documentNumber(documentNumber)
                .accountId(accountId)
                .build();

        // Then
        assertThat(responseOrder1).isEqualTo(responseOrder2);
        assertThat(responseOrder1.accountId()).isEqualTo(accountId);
        assertThat(responseOrder1.documentNumber()).isEqualTo(documentNumber);
    }


    @Test
    void givenAccountResponse_whenSerializedToJson_thenUsesSnakeCasePropertyNames() throws IOException {
        // Given
        AccountResponse response = new AccountResponse(123L, 456L);

        // When
        String json = objectMapper.writeValueAsString(response);
        JsonNode node = objectMapper.readTree(json);

        // Then
        assertThat(node.get("account_id").asLong()).isEqualTo(123L);
        assertThat(node.get("document_number").asLong()).isEqualTo(456L);
        assertThat(node.has("accountId")).isFalse();
        assertThat(node.has("documentNumber")).isFalse();
    }

    @Test
    void givenJsonWithSnakeCaseFields_whenDeserialized_thenAccountResponseIsPopulatedCorrectly() throws IOException {
        // Given
        String json = """
            {
              "account_id": 123,
              "document_number": 456
            }
            """;

        // When
        AccountResponse response = objectMapper.readValue(json, AccountResponse.class);

        // Then
        assertThat(response.accountId()).isEqualTo(123L);
        assertThat(response.documentNumber()).isEqualTo(456L);
    }
}

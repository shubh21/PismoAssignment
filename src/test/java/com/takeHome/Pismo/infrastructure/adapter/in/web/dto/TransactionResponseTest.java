package com.takeHome.Pismo.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionResponseTest {

    private static final long ACCOUNT_ID = 1L;
    private static final long TRANSACTION_ID = 100L;
    private static final BigDecimal AMOUNT = BigDecimal.TEN;
    private static final int OPERATIONTYPE_ID = 2;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void givenValidValues_whenConstructorCalled_thenFieldsAreSetCorrectly() {
        // Given
        LocalDateTime eventDate = LocalDateTime.now();

        // When
        TransactionResponse response = new TransactionResponse(
                TRANSACTION_ID,
                ACCOUNT_ID,
                OPERATIONTYPE_ID,
                AMOUNT,
                eventDate
        );

        // Then
        assertThat(response.transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(response.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.operationTypeId()).isEqualTo(OPERATIONTYPE_ID);
        assertThat(response.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(response.eventDate()).isEqualTo(eventDate);
    }

    @Test
    void givenValidValues_whenBuilderUsed_thenTransactionResponseIsCreatedCorrectly() {
       //given
        LocalDateTime eventDate = LocalDateTime.now();

        // When
        TransactionResponse response = TransactionResponse.builder()
                .transactionId(TRANSACTION_ID)
                .accountId(ACCOUNT_ID)
                .operationTypeId(OPERATIONTYPE_ID)
                .amount(AMOUNT)
                .eventDate(eventDate)
                .build();

        // Then
        assertThat(response.transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(response.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.operationTypeId()).isEqualTo(OPERATIONTYPE_ID);
        assertThat(response.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(response.eventDate()).isEqualTo(eventDate);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 100, 1, 10.00",
            "2, 200, 4, -50.25",
            "3, 300, 7, 0.00"
    })
    void givenMultipleValidCombinations_whenBuilderUsed_thenCorrectValuesAreStored(
            long transactionId,
            long accountId,
            int operationTypeId,
            String amountStr
    ) {
        // Given
        BigDecimal amount = new BigDecimal(amountStr);
        LocalDateTime eventDate = LocalDateTime.now();

        // When
        TransactionResponse response = TransactionResponse.builder()
                .transactionId(transactionId)
                .accountId(accountId)
                .operationTypeId(operationTypeId)
                .amount(amount)
                .eventDate(eventDate)
                .build();

        // Then
        assertThat(response.transactionId()).isEqualTo(transactionId);
        assertThat(response.accountId()).isEqualTo(accountId);
        assertThat(response.operationTypeId()).isEqualTo(operationTypeId);
        assertThat(response.amount()).isEqualByComparingTo(amountStr);
        assertThat(response.eventDate()).isEqualTo(eventDate);
    }


    @Test
    void givenOnlyIdsSet_whenBuilderUsed_thenOtherFieldsDefaultToNullOrZero() {
        // When
        TransactionResponse response = TransactionResponse.builder()
                .transactionId(1L)
                .accountId(2L)
                .build();

        // Then
        assertThat(response.transactionId()).isEqualTo(1L);
        assertThat(response.accountId()).isEqualTo(2L);
        assertThat(response.operationTypeId()).isZero(); // default int
        assertThat(response.amount()).isNull();
        assertThat(response.eventDate()).isNull();
    }

    @Test
    void givenFieldsSetInDifferentOrder_whenBuilderUsed_thenResultIsSame() {
        // Given
        long transactionId = 10L;
        long accountId = 20L;
        int operationTypeId = 3;
        BigDecimal amount = new BigDecimal("50.00");
        LocalDateTime eventDate = LocalDateTime.of(2024, 1, 1, 10, 0);

        // When
        TransactionResponse resp1 = TransactionResponse.builder()
                .transactionId(transactionId)
                .accountId(accountId)
                .operationTypeId(operationTypeId)
                .amount(amount)
                .eventDate(eventDate)
                .build();

        TransactionResponse resp2 = TransactionResponse.builder()
                .eventDate(eventDate)
                .amount(amount)
                .operationTypeId(operationTypeId)
                .accountId(accountId)
                .transactionId(transactionId)
                .build();

        // Then
        assertThat(resp1).isEqualTo(resp2);
        assertThat(resp1.transactionId()).isEqualTo(transactionId);
        assertThat(resp1.accountId()).isEqualTo(accountId);
        assertThat(resp1.operationTypeId()).isEqualTo(operationTypeId);
        assertThat(resp1.amount()).isEqualByComparingTo(amount);
        assertThat(resp1.eventDate()).isEqualTo(eventDate);
    }

    @Test
    void givenTransactionResponse_whenSerializedToJson_thenUsesExpectedJsonPropertyNames() throws Exception {
        // Given
        TransactionResponse response = new TransactionResponse(
                123L,
                456L,
                7,
                new BigDecimal("12.34"),
                LocalDateTime.of(2024, 1, 1, 8, 30)
        );

        // When
        String json = objectMapper.writeValueAsString(response);
        JsonNode node = objectMapper.readTree(json);

        // Then
        assertThat(node.get("transaction_id").asLong()).isEqualTo(123L);
        assertThat(node.get("account_id").asLong()).isEqualTo(456L);
        assertThat(node.get("operationType_Id").asInt()).isEqualTo(7);
        assertThat(node.get("amount").decimalValue()).isEqualByComparingTo("12.34");
        assertThat(node.get("event_date").asText()).isNotBlank();

        // Ensure no camelCase leak for these three
        assertThat(node.has("transactionId")).isFalse();
        assertThat(node.has("accountId")).isFalse();
        assertThat(node.has("operationTypeId")).isFalse();
    }

    @Test
    void givenJsonWithExpectedFields_whenDeserialized_thenTransactionResponseIsPopulatedCorrectly() throws Exception {
        // Given
        String json = """
            {
              "transaction_id": 111,
              "account_id": 222,
              "operationType_Id": 3,
              "amount": 99.99,
              "event_date": "2025-12-07T09:15:00.628748"
            }
            """;
        // When
        TransactionResponse response = objectMapper.readValue(json, TransactionResponse.class);

        // Then
        assertThat(response.transactionId()).isEqualTo(111L);
        assertThat(response.accountId()).isEqualTo(222L);
        assertThat(response.operationTypeId()).isEqualTo(3);
        assertThat(response.amount()).isEqualByComparingTo("99.99");
        assertThat(response.eventDate()).isEqualTo(LocalDateTime.of(2025, 12, 7, 9, 15,0,628_748_000));
    }
}

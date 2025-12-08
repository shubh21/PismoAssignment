package com.takeHome.Pismo.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import static com.takeHome.Pismo.core.Constants.INVALID_ACCOUNT_ID_MSG;
import static com.takeHome.Pismo.core.Constants.INVALID_AMOUNT_VALUE_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateTransactionRequestTest {
    private static final long ACCOUNT_ID = 1L;
    private static final BigDecimal AMOUNT = BigDecimal.TEN;
    private static final int OPERATIONTYPE_ID = 2;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup(){
        objectMapper = new ObjectMapper();
    }

    @Test
    void givenValidValues_whenConstructed_thenFieldsAreSetCorrectly() {
        // When
        CreateTransactionRequest request = new CreateTransactionRequest(ACCOUNT_ID, OPERATIONTYPE_ID, AMOUNT);

        // Then
        assertThat(request.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(request.operationTypeId()).isEqualTo(OPERATIONTYPE_ID);
        assertThat(request.amount()).isEqualByComparingTo(AMOUNT);
    }

    @Test
    void givenNullAmount_whenConstructed_thenThrowException() {
        //Given
        BigDecimal amount = null;

        // When-then
        assertThatThrownBy(()-> new CreateTransactionRequest(ACCOUNT_ID, OPERATIONTYPE_ID, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_AMOUNT_VALUE_MSG.formatted(amount));

    }

    @Test
    void givenZeroAmount_whenConstructed_thenFieldsAreSetCorrectly() {

        CreateTransactionRequest request = new CreateTransactionRequest(ACCOUNT_ID, OPERATIONTYPE_ID, BigDecimal.ZERO);

        // Then
        assertThat(request.accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(request.operationTypeId()).isEqualTo(OPERATIONTYPE_ID);
        assertThat(request.amount()).isEqualByComparingTo(BigDecimal.ZERO);

    }

    @Test
    void givenNegativeAmount_whenConstructed_thenThrowException() {
        //Given
        BigDecimal amount = BigDecimal.valueOf(-15);
        // When-then
        assertThatThrownBy(()-> new CreateTransactionRequest(ACCOUNT_ID, OPERATIONTYPE_ID, BigDecimal.valueOf(-15)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_AMOUNT_VALUE_MSG.formatted(amount));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -3})
    void givenNonPositiveAccountId_whenConstructed_thenThrowException(long accountId) {

        // When-then
        assertThatThrownBy(()-> new CreateTransactionRequest(accountId, OPERATIONTYPE_ID, AMOUNT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_ACCOUNT_ID_MSG.formatted(accountId));
    }

    @Test
    void givenCreateTransactionRequest_whenSerializedToJson_thenUsesSnakeCaseForIds() throws Exception {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest(ACCOUNT_ID, OPERATIONTYPE_ID, AMOUNT);

        // When
        String json = objectMapper.writeValueAsString(request);
        JsonNode node = objectMapper.readTree(json);

        // Then
        assertThat(node.get("account_id").asLong()).isEqualTo(ACCOUNT_ID);
        assertThat(node.get("operation_type_id").asInt()).isEqualTo(OPERATIONTYPE_ID);
        assertThat(node.get("amount").decimalValue()).isEqualByComparingTo(AMOUNT);

        // Ensure no camelCase fields
        assertThat(node.has("accountId")).isFalse();
        assertThat(node.has("operationTypeId")).isFalse();
    }

    @Test
    void givenJsonWithSnakeCaseFields_whenDeserialized_thenCreateTransactionRequestIsPopulatedCorrectly() throws Exception {
        // Given
        String json = """
            {
              "account_id": 999,
              "operation_type_id": 2,
              "amount": 123.45
            }
            """;

        // When
        CreateTransactionRequest request = objectMapper.readValue(json, CreateTransactionRequest.class);

        // Then
        assertThat(request.accountId()).isEqualTo(999L);
        assertThat(request.operationTypeId()).isEqualTo(2);
        assertThat(request.amount()).isEqualByComparingTo("123.45");
    }
}

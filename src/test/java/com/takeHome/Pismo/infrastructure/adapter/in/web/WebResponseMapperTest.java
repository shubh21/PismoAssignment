package com.takeHome.Pismo.infrastructure.adapter.in.web;

import com.takeHome.Pismo.core.contract.output.AccountResult;
import com.takeHome.Pismo.core.contract.output.TransactionResult;
import com.takeHome.Pismo.infrastructure.adapter.in.web.dto.AccountResponse;
import com.takeHome.Pismo.infrastructure.adapter.in.web.dto.TransactionResponse;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class WebResponseMapperTest {

    @Test
    void givenValidAccountResult_whenMappedToAccountResponse_thenFieldsAreMappedCorrectly() {
        // Given
        AccountResult result = new AccountResult(10L,123456789L);

        // When
        AccountResponse response = WebResponseMapper.toAccountResponse(result);

        // Then
        assertThat(response.accountId()).isEqualTo(10L);
        assertThat(response.documentNumber()).isEqualTo(123456789L);
    }


    @Test
    void givenValidTransactionResult_whenMappedToTransactionResponse_thenFieldsAreMappedCorrectly() {
        // Given
        LocalDateTime eventDate = LocalDateTime.of(2024, 1, 1, 12, 0);
        TransactionResult result = new TransactionResult(
                1L,
                2L,
                3,
                new BigDecimal("123.45"),
                eventDate
        );

        // When
        TransactionResponse response = WebResponseMapper.toTransactionResponse(result);

        // Then
        assertThat(response.transactionId()).isEqualTo(1L);
        assertThat(response.accountId()).isEqualTo(2L);
        assertThat(response.operationTypeId()).isEqualTo(3);
        assertThat(response.amount()).isEqualByComparingTo("123.45");
        assertThat(response.eventDate()).isEqualTo(eventDate);
    }

    @Test
    void givenSameInput_whenMappedTwice_thenResultsAreEqualButNotSameInstance() {
        // Given
        AccountResult result = new AccountResult(50L, 1L);

        // When
        AccountResponse accountResponse1 = WebResponseMapper.toAccountResponse(result);
        AccountResponse accountResponse2 = WebResponseMapper.toAccountResponse(result);

        // Then
        assertThat(accountResponse1).isEqualTo(accountResponse2);
        assertThat(accountResponse1).isNotSameAs(accountResponse2);
    }
}

